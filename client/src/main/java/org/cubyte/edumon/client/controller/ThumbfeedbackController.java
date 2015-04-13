package org.cubyte.edumon.client.controller;

import org.cubyte.edumon.client.Main;

public class ThumbfeedbackController implements Controller {
    private Main app;

    @Override
    public void setApp(Main app) {
        this.app = app;
    }
}
