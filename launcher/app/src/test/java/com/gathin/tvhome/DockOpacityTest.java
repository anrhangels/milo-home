package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.*;

public class DockOpacityTest {
    @Test public void aChoiceMadeOnTheTvWinsOverTheTheme() {
        // Theme says 80, the owner picked 30 in Launcher settings.
        assertEquals(30, Config.chosenDockOpacity(30, 80));
        assertEquals(100, Config.chosenDockOpacity(100, 80));
    }

    @Test public void noChoiceKeepsTheThemeValue() {
        // -1 is what Config passes when nothing is saved (or a theme push cleared it).
        assertEquals(80, Config.chosenDockOpacity(-1, 80));
    }

    @Test public void anOutOfRangeChoiceIsIgnored() {
        // Below 30 the band would vanish and the dock lose its shape; the theme key has the same floor.
        assertEquals(80, Config.chosenDockOpacity(0, 80));
        assertEquals(80, Config.chosenDockOpacity(29, 80));
        assertEquals(80, Config.chosenDockOpacity(101, 80));
    }
    @Test public void opacityWorksForSolidCinematicAndGlassMaterials() {
        for(int material:new int[]{255,140,36,13}) {
            assertEquals(material,DockOpacity.alpha(100,material));
            assertEquals(Math.round(material*.5f),DockOpacity.alpha(50,material));
            assertTrue(DockOpacity.alpha(30,material)<DockOpacity.alpha(100,material));
        }
    }
    @Test public void outOfRangeChoicesAreClampedForEveryMaterial() {
        assertEquals(DockOpacity.alpha(30,140),DockOpacity.alpha(-1,140));
        assertEquals(140,DockOpacity.alpha(500,140));
        assertEquals(255,DockOpacity.alpha(100,999));
        assertEquals(0,DockOpacity.alpha(100,-1));
    }
}
