/*
 *  Copyright (C) 2004-2014 Savoir-Faire Linux Inc.
 *
 *  Author: Alexandre Lision <alexandre.lision@savoirfairelinux.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  If you modify this program, or any covered work, by linking or
 *  combining it with the OpenSSL project's OpenSSL library (or a
 *  modified version of that library), containing parts covered by the
 *  terms of the OpenSSL or SSLeay licenses, Savoir-Faire Linux Inc.
 *  grants you additional permission to convey the resulting work.
 *  Corresponding Source for a non-source form of such a combination
 *  shall include the source code for the parts of OpenSSL used as well
 *  as that of the covered work.
 */

package org.sflphone.fragments;

import java.util.Locale;

import org.sflphone.R;
import org.sflphone.service.ISipService;
import org.sflphone.views.ClearableEditText;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class DialingFragment extends Fragment implements OnTouchListener {

    @SuppressWarnings("unused")
    private static final String TAG = DialingFragment.class.getSimpleName();

    ClearableEditText textField;
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * A dummy implementation of the {@link Callbacks} interface that does nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onCallDialed(String to) {
        }

        @Override
        public ISipService getService() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    /**
     * The Activity calling this fragment has to implement this interface
     * 
     */
    public interface Callbacks {
        void onCallDialed(String account);

        public ISipService getService();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.frag_dialing, parent, false);

        textField = (ClearableEditText) inflatedView.findViewById(R.id.textField);
        ((ImageButton) inflatedView.findViewById(R.id.buttonCall)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                String to = textField.getText().toString();
                if (to.contentEquals("")) {
                    textField.setError(getString(R.string.dial_error_no_number_dialed));
                } else {
                    mCallbacks.onCallDialed(to);
                }
            }
        });

        inflatedView.setOnTouchListener(this);

        ((Button) inflatedView.findViewById(R.id.alphabetic_keyboard)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                textField.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                lManager.showSoftInput(textField.getEdit_text(), 0);
            }
        });

        ((Button) inflatedView.findViewById(R.id.numeric_keyboard)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                textField.setInputType(EditorInfo.TYPE_CLASS_PHONE);
                InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                lManager.showSoftInput(textField.getEdit_text(), 0);
            }
        });
        
        textField.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String to = textField.getText().toString();
                if (to.contentEquals("")) {
                    textField.setError(getString(R.string.dial_error_no_number_dialed));
                } else {
                    mCallbacks.onCallDialed(to);
                }
                return true;
            }
        });
        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();
        textField.setTextWatcher(dtmfKeyListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        textField.unsetTextWatcher();
    }

    TextWatcher dtmfKeyListener = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count - before > 1 || count == 0)
                return; // pasted a number (not implemented yet)

            try {
                String toSend = Character.toString(s.charAt(start));
                toSend.toUpperCase(Locale.getDefault());
                mCallbacks.getService().playDtmf(toSend);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        textField.setError(null);
        lManager.hideSoftInputFromWindow(textField.getWindowToken(), 0);
        return false;
    }

}
