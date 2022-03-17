package com.livewire.audax.authentication;

import android.text.method.PasswordTransformationMethod;
import android.view.View;
import org.jetbrains.annotations.NotNull;

public class AsteriskPasswordTransformation extends PasswordTransformationMethod {
    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    private static class PasswordCharSequence implements CharSequence {
        private final CharSequence mSource;

        public PasswordCharSequence(CharSequence source) {
            mSource = source;
        }

        public char charAt(int index) {
            return '*';
        }

        public int length() {
            return mSource.length();
        }

        @NotNull
        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end);
        }
    }
};
