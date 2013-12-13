/* RulesFolowingClosingListener.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */
package net.sourceforge.jnlp.closinglisteners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RulesFolowingClosingListener extends CountingClosingListener {

    private List<Rule<?,String>> rules = new ArrayList<Rule<?,String>>();

    public static class ContainsRule extends StringRule<String> {

        public ContainsRule(String s) {
            super(s);
        }

        @Override
        public boolean evaluate(String upon) {
            return (upon.contains(rule));
        }

        @Override
        public String toPassingString() {
            return "should contain `" + rule + "`";
        }

        @Override
        public String toFailingString() {
            return "should NOT contain `" + rule + "`";
        }
    }

    public static class NotContainsRule extends StringRule<String> {

        public NotContainsRule(String s) {
            super(s);
        }

        @Override
        public boolean evaluate(String upon) {
            return !(upon.contains(rule));
        }

        @Override
        public String toPassingString() {
            return "should NOT contain `" + rule + "`";
        }

        @Override
        public String toFailingString() {
            return "should contain `" + rule + "`";
        }
    }

    public static class MatchesRule extends StringRule<String> {

        public MatchesRule(String s) {
            super(s);
        }

        @Override
        public boolean evaluate(String upon) {
            return (upon.matches(rule));
        }

        @Override
        public String toPassingString() {
            return "should match `" + rule + "`";
        }

        @Override
        public String toFailingString() {
            return "should NOT match `" + rule + "`";
        }
    }

    public static class NotMatchesRule extends StringRule<String> {

        public NotMatchesRule(String s) {
            super(s);
        }

        @Override
        public boolean evaluate(String upon) {
            return !(upon.matches(rule));
        }

        @Override
        public String toPassingString() {
            return "should NOT match`" + rule + "`";
        }

        @Override
        public String toFailingString() {
            return "should match`" + rule + "`";
        }
    }

  
    /**
     *
     * @param rule
     * @return self, to alow chaing add(...).add(..)...
     */
    public RulesFolowingClosingListener addMatchingRule(String rule) {
        this.rules.add(new MatchesRule(rule));
        return this;
    }

    /**
     *
     * @param rule
     * @return self, to alow chaing add(...).add(..)...
     */
    public RulesFolowingClosingListener addNotMatchingRule(String rule) {
        this.rules.add(new NotMatchesRule(rule));
        return this;
    }

    /**
     *
     * @param rule
     * @return self, to alow chaing add(...).add(..)...
     */
    public RulesFolowingClosingListener addContainsRule(String rule) {
        this.rules.add(new ContainsRule(rule));
        return this;
    }

    /**
     *
     * @param rule
     * @return self, to alow chaing add(...).add(..)...
     */
    public RulesFolowingClosingListener addNotContainsRule(String rule) {
        this.rules.add(new NotContainsRule(rule));
        return this;
    }

    public RulesFolowingClosingListener() {
    }

    public RulesFolowingClosingListener(List<Rule<?,String>> l) {
        addRules(l);
    }

    public RulesFolowingClosingListener(Rule<?,String>... l) {
        addRules(l);
    }

    public List<Rule<?,String>> getRules() {
        return rules;
    }

    public void setRules(List<Rule<?,String>> rules) {
        if (rules == null) {
            throw new NullPointerException("rules cant be null");
        }
        this.rules = rules;
    }

    /**
     * no more rules will be possible to add by doing this
     * @param rules
     */
    public void setRules(Rule<?,String>[] rules) {
        if (rules == null) {
            throw new NullPointerException("rules cant be null");
        }
        this.rules = Arrays.asList(rules);
    }

     final public RulesFolowingClosingListener addRules(List<Rule<?,String>> rules) {
        if (rules == null) {
            throw new NullPointerException("rules cant be null");
        }
        this.rules.addAll(rules);
        return this;
    }

    final public RulesFolowingClosingListener addRules(Rule<?,String>... rules) {
        if (rules == null) {
            throw new NullPointerException("rules cant be null");
        }
        this.rules.addAll(Arrays.asList(rules));
        return this;
    }

    @Override
    protected boolean isAlowedToFinish(String content) {
        if (rules == null || rules.size() < 1) {
            throw new IllegalStateException("No rules specified");
        }
        for (Rule<?,String> rule : rules) {
            if (!rule.evaluate(content)) {
                return false;
            }
        }
        return true;


    }
}
