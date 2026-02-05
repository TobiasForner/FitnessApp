package com.example.fitnessapp3.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class PositiveNegativeDialogFragment extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }

    public PositiveNegativeDialogFragment(int messageID, int positiveTextID, int negativeTextID, String messageExtra, int version) {
        this.messageID = messageID;
        this.positiveTextID = positiveTextID;
        this.negativeTextID = negativeTextID;
        this.messageExtra = messageExtra;
        this.version = version;
    }


    // Use this instance of the interface to deliver action events
    NoticeDialogListener listener;
    final int messageID;
    final int positiveTextID;
    final int negativeTextID;
    private String message;
    private final String messageExtra;
    private final int version;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(positiveTextID, (dialog, id) -> listener.onDialogPositiveClick(PositiveNegativeDialogFragment.this))
                .setNegativeButton(negativeTextID, (dialog, id) -> listener.onDialogNegativeClick(PositiveNegativeDialogFragment.this));
        // Create the AlertDialog object and return it
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeDialogListener) context;
            this.message = getString(messageID, messageExtra);
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context
                    + " must implement NoticeDialogListener");
        }
    }

    public int getVersion() {
        return version;
    }

    public String getMessageExtra() {
        return messageExtra;
    }
}
