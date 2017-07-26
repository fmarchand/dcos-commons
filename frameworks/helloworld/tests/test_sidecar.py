import pytest

import sdk_install as install
import sdk_plan
import sdk_utils
import sdk_marathon

from tests.config import (
    PACKAGE_NAME
)


@pytest.fixture(scope='module', autouse=True)
def configure_package(configure_universe):
    try:
        install.uninstall(PACKAGE_NAME)
        options = {
            "service": {
                "spec_file": "examples/sidecar.yml"
            }
        }

        # this yml has 2 hello's + 0 world's:
        install.install(PACKAGE_NAME, 2, additional_options=options)

        yield # let the test session execute
    finally:
        install.uninstall(PACKAGE_NAME)


@pytest.mark.sanity
def test_deploy():
    sdk_plan.wait_for_completed_deployment(PACKAGE_NAME)
    deployment_plan = sdk_plan.get_deployment_plan(PACKAGE_NAME)
    sdk_utils.out("deployment plan: " + str(deployment_plan))

    assert(len(deployment_plan['phases']) == 2)
    assert(deployment_plan['phases'][0]['name'] == 'server-deploy')
    assert(deployment_plan['phases'][1]['name'] == 'once-deploy')
    assert(len(deployment_plan['phases'][0]['steps']) == 2)
    assert(len(deployment_plan['phases'][1]['steps']) == 2)


@pytest.mark.sanity
def test_sidecar():
    run_plan('sidecar')


@pytest.mark.sanity
def test_sidecar_parameterized():
    run_plan('sidecar-parameterized', {'PLAN_PARAMETER': 'parameterized'})

@pytest.mark.sanity
def test_toxic_sidecar_doesnt_trigger_recovery():
    # 1. Run the toxic sidecar plan that will never succeed.
    # 2. Restart the scheduler.
    # 3. Verify that its recovery plan is empty, as a failed FINISHED task should
    # never trigger recovery
    recovery_plan = sdk_plan.get_plan(PACKAGE_NAME, 'recovery')
    assert(len(recovery_plan['phases']) == 0)
    sdk_utils.out(recovery_plan)
    run_plan('sidecar-toxic', wait_for_completion=False)
    # Wait for the bad sidecar plan to be starting.
    sdk_plan.wait_for_starting_plan(PACKAGE_NAME, 'sidecar-toxic')

    # Restart the scheduler and wait for it to come up.
    sdk_marathon.restart_app(PACKAGE_NAME)
    sdk_plan.wait_for_completed_deployment(PACKAGE_NAME)

    # Now, verify that its recovery plan is empty.
    sdk_plan.wait_for_completed_plan(PACKAGE_NAME, 'recovery')
    recovery_plan = sdk_plan.get_plan(PACKAGE_NAME, 'recovery')
    assert(len(recovery_plan['phases']) == 0)


def run_plan(plan_name, wait_for_completion=True, params=None):
    sdk_plan.start_plan(PACKAGE_NAME, plan_name, params)

    started_plan = sdk_plan.get_plan(PACKAGE_NAME, plan_name)
    sdk_utils.out("sidecar plan: " + str(started_plan))
    assert(len(started_plan['phases']) == 1)
    assert(started_plan['phases'][0]['name'] == plan_name + '-deploy')
    assert(len(started_plan['phases'][0]['steps']) == 2)

    if wait_for_completion:
        sdk_plan.wait_for_completed_plan(PACKAGE_NAME, plan_name)
