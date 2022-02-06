/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:04
 *  
 */

package pw.appcode.mimic;

import org.junit.Test;

import static org.junit.Assert.*;

public class UIContainerTest {

    @Test
    public void main() {
        UIContainer uiContainer = new UIContainer(null, "layout 'hbox' \n items [ \n{ \nxtype 'container' \nlayout 'vbox' \n} \n{ \nxtype 'container' \nlayout 'hbox' \n}\n]");
        assertEquals(uiContainer.getOrientation(), "hbox");
        assertEquals(uiContainer.getItems().length, 2);
    }
}