//
// Copyright (c) 2024 Satoshi Fukutomi.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//

package com.fuktommy.pinencrypter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private final static int MENU_ITEM_ABOUT = 0;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar tb = findViewById(R.id.my_toolbar);
        setSupportActionBar(tb);

        setEncodeButtonClickListener();
        setDecodeButtonClickListener();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuItem aboutItem = menu.add(0, MENU_ITEM_ABOUT, 0, R.string.about);
        aboutItem.setIcon(android.R.drawable.ic_menu_info_details);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == MENU_ITEM_ABOUT) {
            displayAbout();
            return true;
        }
        return true;
    }

    private void setEncodeButtonClickListener() {
        final Button button = findViewById(R.id.encode_button);
        button.setOnClickListener(v -> {
            try {
                encode();
            } catch (Exception e) {
                displayError(new Handler(), e.getMessage());
            }
        });
    }

    private void setDecodeButtonClickListener() {
        final Button button = findViewById(R.id.decode_button);
        button.setOnClickListener(v -> {
            try {
                decode();
            } catch (Exception e) {
                displayError(new Handler(), e.getMessage());
            }
        });
    }

    private String getTextViewValue(final int id) {
        return ((TextView) findViewById(id)).getText().toString();
    }

    private void setTextViewValue(final int id, final String value) {
        ((TextView) findViewById(id)).setText(value);
    }

    private void encode() throws InvalidKeyException, NoSuchAlgorithmException {
        final String pin = getTextViewValue(R.id.pin_field);
        final String key = getTextViewValue(R.id.key_field);
        final String result = new Encoder().encode(key, pin);
        setTextViewValue(R.id.result_field, result);
    }

    private void decode() {
        final String pin = getTextViewValue(R.id.pin_field);
        final String key = getTextViewValue(R.id.key_field);
        final Handler handler= new Handler();
        handler.post(() -> setTextViewValue(R.id.result_field, getText(R.string.wait_message).toString()));
        new Thread(() -> {
            try {
                final List<List<String>> result = new Encoder().decode(key, pin);
                final List<String> lines = new ArrayList<>();
                for (final List<String> list : result) {
                    if (list.isEmpty()) {
                        lines.add(getText(R.string.not_found_message).toString());
                    } else {
                        lines.add(String.join(" ", list));
                    }
                }
                handler.post(() -> setTextViewValue(R.id.result_field, String.join("\n", lines)));
            } catch (Exception e) {
                displayError(handler, e.getMessage());
            }
        }).start();
    }

    private void displayDialog(final Handler handler, final int title, final String message) {
        handler.post(() -> {
            final DialogInterface.OnClickListener ocl
                    = (dialog, whichButton) -> setResult(RESULT_OK);
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setNeutralButton(R.string.ok, ocl)
                    .create().show();
        });
    }

    private void displayAbout() {
        final String message = (getText(R.string.about_message)
                + "\n\n\n"
                + getText(R.string.license))
            .replaceAll("\n +", "\n");
        displayDialog(new Handler(), R.string.about, message);
    }

    private void displayError(final Handler handler, final String message) {
        displayDialog(handler, R.string.error, message);
    }
}
