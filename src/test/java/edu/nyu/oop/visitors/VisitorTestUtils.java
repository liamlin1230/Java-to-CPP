package edu.nyu.oop.visitors;

import java.util.Map;

import edu.nyu.oop.constructs.JIdentifier;

public class VisitorTestUtils {

    public static JIdentifier findMemberId(Map<JIdentifier, Object> members, String name) {

        for (JIdentifier i : members.keySet()) {
            if (i.getName().equals(name))
                return i;
        }

        return null; //not found
    }

}
