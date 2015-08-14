package com.lasarobotics.ftc.doodle.actions;

import com.lasarobotics.ftc.doodle.DoodleRunData;

/**
 * Dummy action that does absolutely nothing but waste precious disk space.
 * It's a great starting template though.
 */
public class NoOperation extends Action {

    public NoOperation()
    {
        super("Do nothing");
    }

    public void run(DoodleRunData data)
    {

    }

    public String toString()
    {
        return "Do nothing";
    }
}