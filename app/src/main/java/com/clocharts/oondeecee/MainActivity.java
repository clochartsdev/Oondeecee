package com.clocharts.oondeecee;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Activity actvt;
    Dialog settingsDialog;
    View tappedView;
    HashMap<Integer,List<Button>> buttonsMap;
    boolean gameOver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actvt = this; // Definizione activity globale

        // Build griglia bottoni
        buildButtonsTable(getDisplayMetrics().x/12);
    }

    private void buildButtonsTable(int cellSizeXY){

        // Init mappa dei bottoni
        buttonsMap = new HashMap<>();

        // Build taple layout
        TableLayout mTableLayout = ((TableLayout) findViewById(R.id.gridTableLayout));

        mTableLayout.removeAllViews(); // clear

        for (int count = 0; count < 11; count++) {

            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

            ArrayList <Button> buttonsList = new ArrayList<>(); // Init lista dei bottoni

            for (int b = 0; b < 11; b++) {
                Button btn = new Button(this);
                btn.setBackgroundResource(R.drawable.cellbg);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!gameOver && view.getTag() == null) {
                            tappedView = view;
                            settingsDialog = new Dialog(actvt);
                            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                            settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.buttons_layout
                                    , null));
                            settingsDialog.show();
                        }
                    }
                });

                TableRow.LayoutParams btnParams = new TableRow.LayoutParams(cellSizeXY,cellSizeXY);
                btnParams.setMargins(1,1,1,1);
                btn.setLayoutParams(btnParams);
                btn.setTextSize(cellSizeXY/4);
                btn.setPadding(0,0,0,0);
                row.addView(btn);

                // Add button to horizontal list
                buttonsList.add(btn);
            }

            // Add horizontal button list to map
            buttonsMap.put(count, buttonsList);

            mTableLayout.addView(row);
        }
    }

    private void giocatoreIntelligiente(){

        boolean computerwin = false;

        searchundici:
        {
            for (int i = 0; i < 11; i++) {
                for (Button btn : getHorizontalButtonByRow(i)) {
                    if (btn.getTag() == null) {
                        for (int n = 1; n <= 3; n++) {
                            btn.setTag(n);
                            if (checkGameStatus(false)) {
                                btn.setText("" + n);
                                btn.setTextColor(Color.RED);
                                computerwin = true;
                                tappedView = btn;
                                break searchundici;
                            }
                            btn.setTag(null);
                        }
                    }
                }
            }

            for (int i = 0; i < 11; i++) {
                for (Button btn : getVerticalButtonByColumn(i)) {
                    if (btn.getTag() == null) {
                        for (int n = 1; n <= 3; n++) {
                            btn.setTag(n);
                            if (checkGameStatus(false)) {
                                btn.setText("" + n);
                                btn.setTextColor(Color.RED);
                                computerwin = true;
                                tappedView = btn;
                                break searchundici;
                            }
                            btn.setTag(null);
                        }
                    }
                }
            }
        }

        if (!computerwin) {
            boolean isMoved = false;
            computermove:
            {
                for (Button btn : getArrangedButtonList()) {
                    if (btn.getTag() == null) {
                        for (int n = 3; n >= 1; n--) {
                            btn.setTag(n);

                            boolean opponentswin = false;

                            evaluateopponent:
                            {
                                /// valuta l'eventuale mossa dell'avversario
                                for (int ai = 0; ai < 11; ai++) {
                                    for (Button abtn : getHorizontalButtonByRow(ai)) {
                                        if (abtn.getTag() == null) {
                                            for (int an = 3; an >= 1; an--) {
                                                abtn.setTag(an);
                                                if (checkGameStatus(false)) {
                                                    opponentswin = true;
                                                    abtn.setTag(null);
                                                    break evaluateopponent;
                                                }
                                                abtn.setTag(null);
                                            }
                                        }
                                    }
                                }
                            }

                            if (!opponentswin) {
                                btn.setText("" + n);
                                btn.setTextColor(Color.RED);
                                isMoved = true;
                                tappedView = btn;
                                break computermove;
                            }

                            btn.setTag(null);
                        }
                    }
                }
            }
            if (!isMoved){
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Mooooh");
                alertDialog.setMessage("Mooh, qualsiasi mossa faccio ho perso, abbandono");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                gameOver = true;
            }
        } else {
            gameOver = true;
        }
    }

    private boolean checkGameStatus(boolean updategui){

        if (updategui) {
            cleanAllCellsBackground();
            if (tappedView != null) tappedView.setBackgroundResource(R.drawable.solidwhite_border);
        }

        boolean undici = false;

        // Controllo tutte le righe orizzontali
        for (int i = 0 ; i < 11 ; i++) {
            if (checkCells(getHorizontalButtonByRow(i), updategui)) undici = true;
        }

        // Controllo tutte le colonne verticali
        for (int i = 0 ; i < 11 ; i++){
            if (checkCells(getVerticalButtonByColumn(i), updategui)) undici = true;
        }

        return undici;
    }

    private boolean checkCells(List<Button> buttonsList, boolean updategui){

        ArrayList<Button> checkResult = new ArrayList<>();
        int checkCount = 0;
        boolean undici = false;

        for (int i = 0 ; i < buttonsList.size() ; i++){

            Button btn = buttonsList.get(i);
            if (btn.getTag() != null) {
                checkResult.add(btn);
                checkCount += (Integer) btn.getTag();
            }

            if ((checkCount > 0 && btn.getTag() == null) || i == buttonsList.size()-1) { // Delimitatore vuoto o fine tabella
                if (checkCount == 11) { // Valida, trovato 11 singolo - UNDICI
                    undici = true;
                }
            }

            if (undici){
                if (updategui) {
                    // Colora i bottoni
                    for (Button undiciBtn : checkResult) {
                        undiciBtn.setBackgroundResource(R.drawable.solidgreen);
                        if (undiciBtn == tappedView) undiciBtn.setBackgroundResource(R.drawable.solidgreen_border);
                    }
                    break;
                }
            }

            // reset
            if (btn.getTag() == null){
                checkResult = new ArrayList<>();
                checkCount = 0;
            }
        }
        return undici;
    }

    private void cleanAllCellsBackground(){
        if (buttonsMap != null){
            for (Map.Entry<Integer, List<Button>> entry : buttonsMap.entrySet()){
                for (Button btn : entry.getValue()){
                    btn.setBackgroundResource(R.drawable.cellbg);
                }
            }
        }
    }

    private List<Button> getArrangedButtonList(){

        ArrayList<Button> retList = new ArrayList<>();
        ArrayList<Button> lowerPriority = new ArrayList();
        for (int y = 0 ; y < 11 ; y++){
            for (int x = 0 ; x < 11 ; x++){
                boolean eligible = false;
                Button btn = getButtonAtCoord(x,y);
                if (btn.getTag() == null) {
                    if (x > 0 && getButtonAtCoord(x-1,y).getTag() != null) eligible = true;
                    if (x < 10 && getButtonAtCoord(x+1,y).getTag() != null) eligible = true;
                    if (y > 0 && getButtonAtCoord(x, y-1).getTag() != null) eligible = true;
                    if (y < 10 && getButtonAtCoord(x, y+1).getTag() != null) eligible = true;
                    if (eligible){
                        if (!retList.contains(btn)) retList.add(btn);
                    } else {
                        if (!lowerPriority.contains(btn)) lowerPriority.add(btn);
                    }
                }
            }
        }
        Collections.shuffle(lowerPriority);
        retList.addAll(lowerPriority);
        return retList;
    }

    private Button getButtonAtCoord(Integer row, Integer column){
        if (buttonsMap != null) return buttonsMap.get(row).get(column);
        return null;
    }

    private List<Button> getHorizontalButtonByRow(Integer row){
        if (buttonsMap != null) return buttonsMap.get(row);
        return null;
    }

    private List<Button> getVerticalButtonByColumn(Integer column){
        if (buttonsMap != null){
            ArrayList<Button> retBtnList = new ArrayList<>();
            for (int i = 0 ; i < 11 ; i++) {
                retBtnList.add(buttonsMap.get(i).get(column));
            }
            return retBtnList;
        }
        return null;
    }

    private Point getDisplayMetrics(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public void dismissListener(View view){
        Button dlgButton = (Button) view;

        ((Button) tappedView).setText(dlgButton.getText());
        tappedView.setTag(Integer.parseInt(dlgButton.getText().toString()));

        giocatoreIntelligiente();
        checkGameStatus(true); // Controlla le righe e le colonne

        if (settingsDialog != null) settingsDialog.dismiss(); // Dialog dismiss
    }

    public void resetGrid(View view){
        // Build griglia bottoni
        buildButtonsTable(getDisplayMetrics().x/12);
        gameOver = false;
    }

    public void forzaMossa(View view){
        giocatoreIntelligiente();
        checkGameStatus(true); // Controlla le righe e le colonne
    }
}
