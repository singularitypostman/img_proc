package com.freaks.client;

import com.ericsson.otp.erlang.*;
import com.freaks.Settings;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Created by proxeld on 10.01.15.
 */
public class Controller {
    View view;
    NodeClient erlangNodeClient;
    Logger logger = Logger.getLogger("controllerLogger");

    public Controller() {
        erlangNodeClient = new NodeClient(Settings.JAVA_SERVER_NODE_NAME, Settings.JAVA_SERVER_COOKIE, "clientNode");
    }

    public void setView(View _view) {
        if(_view != null)
            view = _view;
        else
            throw new IllegalArgumentException("View cannot be null!");
    }

    public void onConnect() {
        if(erlangNodeClient.isConnected()) {
            view.showInfoPopup("You are already connected.");
            return;
        }

        try {
            erlangNodeClient.connect();
        } catch (TimeoutException e) {
            view.showErrorPopup("Connection timeout... Maybe server node it is not running?");
        } catch (IOException e) {
            view.showErrorPopup("Cannot connect to server node... Maybe it is not running?");
        } catch (OtpAuthException e) {
            view.showInfoPopup("Connection refused by remote node. Check your setting (Setting.java).");
        }
    }

    public void onSave(BufferedImage image, File imageFile) {
        if(image == null) {
            view.showInfoPopup("No image to be saved. Execute some operation.");
            return;
        }

        try {
            ImageIO.write(image, "png", imageFile);
        } catch (IOException e) {
            view.showErrorPopup("Error while saving image. Try again.");
        }
    }

    public void onOperationChosen(BufferedImage image, String operation) {
        if(!erlangNodeClient.isConnected()) {
            view.showInfoPopup("You must establish connection first");
            return;
        } else if(image == null) {
            view.showInfoPopup("Choose image and then select operation");
            return;
        }

        // MESSAGE STRUCTURE
        // OtpErlangTuple(sender: OtpErlangPid, image: OtpErlangBinary, operation: OtpErlangString)

        OtpErlangTuple data = new OtpErlangTuple(
                new OtpErlangObject[] {
                        new OtpErlangBinary(new ImageIcon(image)),
                        new OtpErlangString(operation)
                }
        );

        try {
            erlangNodeClient.sendMessage(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            OtpErlangObject response = erlangNodeClient.receiveResponse();
            OtpErlangBinary processedImageBinary = (OtpErlangBinary) (response);
            ImageIcon processedImage = (ImageIcon)(processedImageBinary.getObject());

            view.setProcessedImage(processedImage);
            view.displayProccessedImage(processedImage);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (OtpErlangExit otpErlangExit) {
            otpErlangExit.printStackTrace();
        } catch (OtpAuthException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dump_table(Integer[][] tab) {
        for(int row = 0; row < tab.length; ++row) {
            for(int column = 0; column < tab[row].length; ++column) {
                System.out.print(tab[row][column].toString() + ", ");
            }
            System.out.println();
        }
    };

    private static int[] getPixelData(BufferedImage img, int x, int y) {
        int argb = img.getRGB(x, y);

        int rgb[] = new int[] {
                (argb >> 16) & 0xff, // red
                (argb >> 8) & 0xff, // green
                (argb) & 0xff  // blue

        };

        return rgb;
    }
}