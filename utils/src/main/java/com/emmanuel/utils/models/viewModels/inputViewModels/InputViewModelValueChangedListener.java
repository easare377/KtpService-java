package com.emmanuel.utils.models.viewModels.inputViewModels;

public interface InputViewModelValueChangedListener<T> {
    void onValueChanged(T value);
    void onErrorMessageChanged(String errorMessage);
}
