package org.apache.mesos.offer.constrain;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Tests for {@link MarathonConstraintParser}.
 */
public class MarathonConstraintParserTest {

    @Test
    public void testSplitConstraints() throws IOException {
        assertEquals(Arrays.asList(Arrays.asList("")),
                MarathonConstraintParser.splitConstraints(""));
        assertEquals(Arrays.asList(Arrays.asList("a")),
                MarathonConstraintParser.splitConstraints("a"));
        assertEquals(Arrays.asList(Arrays.asList("a", "b", "c")),
                MarathonConstraintParser.splitConstraints(unescape("['a', 'b', 'c']")));
        assertEquals(Arrays.asList(Arrays.asList("a", "b", "c")),
                MarathonConstraintParser.splitConstraints(unescape("[['a', 'b', 'c']]")));
        assertEquals(Arrays.asList(Arrays.asList("a", "b", "c"), Arrays.asList("d", "e")),
                MarathonConstraintParser.splitConstraints(unescape("[['a', 'b', 'c'], ['d', 'e']]")));
        assertEquals(Arrays.asList(Arrays.asList("a"), Collections.emptyList()),
                MarathonConstraintParser.splitConstraints(unescape("[['a'], []]")));
        assertEquals(Arrays.asList(Arrays.asList("a", "b", "c"), Arrays.asList("d", "e"), Arrays.asList("f", "g", "")),
                MarathonConstraintParser.splitConstraints("a:b:c,d:e,f:g:"));
        assertEquals(Arrays.asList(Arrays.asList("a", "b", "c"), Arrays.asList("", "d", "e"), Arrays.asList("", "f", "")),
                MarathonConstraintParser.splitConstraints("a:b:c,:d:e,:f:"));
        assertEquals(Arrays.asList(Arrays.asList("a", "b", "c"), Arrays.asList("", "d", "e"), Arrays.asList("", "f", "")),
                MarathonConstraintParser.splitConstraints(" a : b : c , : d : e , : f : "));
        assertEquals(Arrays.asList(Arrays.asList("", "", ""), Arrays.asList("", "")),
                MarathonConstraintParser.splitConstraints("::,:"));
    }

    @Test
    public void testUniqueOperator() throws IOException {
        // example from marathon documentation:
        String constraintStr = MarathonConstraintParser.parse(unescape("[['hostname', 'UNIQUE']]")).toString();
        assertEquals("MaxPerHostnameRule{max=1, task_filter=AnyMatcher{}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['hostname', 'UNIQUE']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("hostname:UNIQUE").toString());

        constraintStr = MarathonConstraintParser.parse(unescape("[['rack_id', 'UNIQUE']]")).toString();
        assertEquals("AndRule{rules=[AttributeRule{matcher=RegexMatcher{pattern='rack_id:.*'}}, " +
                "MaxPerAttributeRule{max=1, matcher=RegexMatcher{pattern='rack_id:.*'}, task_filter=AnyMatcher{}}]}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['rack_id', 'UNIQUE']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("rack_id:UNIQUE").toString());
    }

    @Test
    public void testClusterOperator() throws IOException {
        // example from marathon documentation:
        String constraintStr = MarathonConstraintParser.parse(unescape("[['rack_id', 'CLUSTER', 'rack-1']]")).toString();
        assertEquals("AttributeRule{matcher=ExactMatcher{str='rack_id:rack-1'}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['rack_id', 'CLUSTER', 'rack-1']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("rack_id:CLUSTER:rack-1").toString());

        // example from marathon documentation:
        constraintStr = MarathonConstraintParser.parse(unescape("[['hostname', 'CLUSTER', 'a.specific.node.com']]")).toString();
        assertEquals("HostnameRule{matcher=ExactMatcher{str='a.specific.node.com'}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['hostname', 'CLUSTER', 'a.specific.node.com']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("hostname:CLUSTER:a.specific.node.com").toString());
    }

    @Test
    public void testGroupByOperator() throws IOException {
        // example from marathon documentation:
        String constraintStr = MarathonConstraintParser.parse(unescape("[['rack_id', 'GROUP_BY']]")).toString();
        assertEquals("RoundRobinByAttributeRule{attribute=rack_id, attribute_count=Optional.empty, task_filter=AnyMatcher{}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['rack_id', 'GROUP_BY']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("rack_id:GROUP_BY").toString());

        // example from marathon documentation:
        constraintStr = MarathonConstraintParser.parse(unescape("[['rack_id', 'GROUP_BY', '3']]")).toString();
        assertEquals("RoundRobinByAttributeRule{attribute=rack_id, attribute_count=Optional[3], task_filter=AnyMatcher{}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['rack_id', 'GROUP_BY', '3']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("rack_id:GROUP_BY:3").toString());

        constraintStr = MarathonConstraintParser.parse(unescape("[['hostname', 'GROUP_BY']]")).toString();
        assertEquals("RoundRobinByHostnameRule{agent_count=Optional.empty, task_filter=AnyMatcher{}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['hostname', 'GROUP_BY']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("hostname:GROUP_BY").toString());

        constraintStr = MarathonConstraintParser.parse(unescape("[['hostname', 'GROUP_BY', '3']]")).toString();
        assertEquals("RoundRobinByHostnameRule{agent_count=Optional[3], task_filter=AnyMatcher{}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['hostname', 'GROUP_BY', '3']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("hostname:GROUP_BY:3").toString());
    }

    @Test
    public void testLikeOperator() throws IOException {
        // example from marathon documentation:
        String constraintStr = MarathonConstraintParser.parse(unescape("[['rack_id', 'LIKE', 'rack-[1-3]']]")).toString();
        assertEquals("AttributeRule{matcher=RegexMatcher{pattern='rack_id:rack-[1-3]'}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['rack_id', 'LIKE', 'rack-[1-3]']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("rack_id:LIKE:rack-[1-3]").toString());

        constraintStr = MarathonConstraintParser.parse(unescape("[['hostname', 'LIKE', 'rack-[1-3]']]")).toString();
        assertEquals("HostnameRule{matcher=RegexMatcher{pattern='rack-[1-3]'}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['hostname', 'LIKE', 'rack-[1-3]']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("hostname:LIKE:rack-[1-3]").toString());
    }

    @Test
    public void testUnlikeOperator() throws IOException {
        // example from marathon documentation:
        String constraintStr = MarathonConstraintParser.parse(unescape("[['rack_id', 'UNLIKE', 'rack-[7-9]']]")).toString();
        assertEquals("NotRule{rule=AttributeRule{matcher=RegexMatcher{pattern='rack_id:rack-[7-9]'}}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['rack_id', 'UNLIKE', 'rack-[7-9]']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("rack_id:UNLIKE:rack-[7-9]").toString());

        constraintStr = MarathonConstraintParser.parse(unescape("[['hostname', 'UNLIKE', 'rack-[7-9]']]")).toString();
        assertEquals("NotRule{rule=HostnameRule{matcher=RegexMatcher{pattern='rack-[7-9]'}}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['hostname', 'UNLIKE', 'rack-[7-9]']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("hostname:UNLIKE:rack-[7-9]").toString());
    }

    @Test
    public void testMaxPerOperator() throws IOException {
        // example from marathon documentation:
        String constraintStr = MarathonConstraintParser.parse(unescape("[['rack_id', 'MAX_PER', '2']]")).toString();
        assertEquals("AndRule{rules=[AttributeRule{matcher=RegexMatcher{pattern='rack_id:.*'}}, " +
                "MaxPerAttributeRule{max=2, matcher=RegexMatcher{pattern='rack_id:.*'}, task_filter=AnyMatcher{}}]}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['rack_id', 'MAX_PER', '2']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("rack_id:MAX_PER:2").toString());

        constraintStr = MarathonConstraintParser.parse(unescape("[['hostname', 'MAX_PER', '2']]")).toString();
        assertEquals("MaxPerHostnameRule{max=2, task_filter=AnyMatcher{}}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(unescape("['hostname', 'MAX_PER', '2']")).toString());
        assertEquals(constraintStr, MarathonConstraintParser.parse("hostname:MAX_PER:2").toString());
    }

    @Test
    public void testManyOperators() throws IOException {
        String constraintStr = MarathonConstraintParser.parse(unescape(
                "[['hostname', 'UNIQUE'], "
                + "['rack_id', 'CLUSTER', 'rack-1'], "
                + "['rack_id', 'GROUP_BY'], "
                + "['rack_id', 'LIKE', 'rack-[1-3]'], "
                + "['rack_id', 'UNLIKE', 'rack-[7-9]'],"
                + "['rack_id', 'MAX_PER', '2']]")).toString();
        assertEquals("AndRule{"
                + "rules=[MaxPerHostnameRule{max=1, task_filter=AnyMatcher{}}, "
                + "AttributeRule{matcher=ExactMatcher{str='rack_id:rack-1'}}, "
                + "RoundRobinByAttributeRule{attribute=rack_id, attribute_count=Optional.empty, task_filter=AnyMatcher{}}, "
                + "AttributeRule{matcher=RegexMatcher{pattern='rack_id:rack-[1-3]'}}, "
                + "NotRule{rule=AttributeRule{matcher=RegexMatcher{pattern='rack_id:rack-[7-9]'}}}, "
                + "AndRule{rules=["
                + "AttributeRule{matcher=RegexMatcher{pattern='rack_id:.*'}}, "
                + "MaxPerAttributeRule{max=2, matcher=RegexMatcher{pattern='rack_id:.*'}, task_filter=AnyMatcher{}}]}]}", constraintStr);
        assertEquals(constraintStr, MarathonConstraintParser.parse(
                "hostname:UNIQUE,"
                + "rack_id:CLUSTER:rack-1,"
                + "rack_id:GROUP_BY,"
                + "rack_id:LIKE:rack-[1-3],"
                + "rack_id:UNLIKE:rack-[7-9],"
                + "rack_id:MAX_PER:2").toString());
    }

    @Test
    public void testEmptyConstraint() throws IOException {
        assertEquals("PassthroughRule{}", MarathonConstraintParser.parse("").toString());
    }

    @Test(expected = IOException.class)
    public void testBadListConstraint() throws IOException {
        MarathonConstraintParser.parse(unescape("[['rack_id', 'MAX_PER', '2'")); // missing ']]'
    }

    @Test(expected = IOException.class)
    public void testBadFlatConstraint() throws IOException {
        MarathonConstraintParser.parse("rack_id:MAX_PER:,"); // missing last elem
    }

    @Test(expected = IOException.class)
    public void testBadParamGroupBy() throws IOException {
        MarathonConstraintParser.parse("rack_id:GROUP_BY:foo"); // expected int
    }

    @Test(expected = IOException.class)
    public void testBadParamMaxPer() throws IOException {
        MarathonConstraintParser.parse("rack_id:MAX_PER:foo"); // expected int
    }

    @Test(expected = IOException.class)
    public void testMissingParamCluster() throws IOException {
        MarathonConstraintParser.parse("rack_id:CLUSTER"); // expected param
    }

    @Test(expected = IOException.class)
    public void testMissingParamLike() throws IOException {
        MarathonConstraintParser.parse("rack_id:LIKE"); // expected param
    }

    @Test(expected = IOException.class)
    public void testMissingParamUnlike() throws IOException {
        MarathonConstraintParser.parse("rack_id:UNLIKE"); // expected param
    }

    @Test(expected = IOException.class)
    public void testMissingParamMaxPer() throws IOException {
        MarathonConstraintParser.parse("rack_id:MAX_PER"); // expected param
    }

    @Test(expected = IOException.class)
    public void testUnknownCommand() throws IOException {
        MarathonConstraintParser.parse("rack_id:FOO:foo");
    }

    @Test(expected = IOException.class)
    public void testTooManyElemenents() throws IOException {
        MarathonConstraintParser.parse("rack_id:LIKE:foo:bar");
    }

    // Avoid needing \"'s throughout json strings:
    private static String unescape(String s) {
        return s.replace('\'', '"');
    }
}