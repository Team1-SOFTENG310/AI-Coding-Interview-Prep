package com.aicodinginterviewprep.controller;

import com.aicodinginterviewprep.AppContext;
import com.aicodinginterviewprep.ScreenController;
import javafx.fxml.FXML;

public class HomeController implements ScreenController {

    private AppContext context;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
    }

    @FXML
    private void onGetStarted() {
        context.showPractice();
    }

    @FXML
    private void onSignIn() {
        context.showAuthentication();
    }
}
