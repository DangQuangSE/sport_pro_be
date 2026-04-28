package com.sport_pro_be.auth.forgotpassword.interfaces;

import com.sport_pro_be.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.sport_pro_be.auth.forgotpassword.dto.ForgotPasswordTokenResponse;
import com.sport_pro_be.auth.forgotpassword.dto.ResetPasswordRequest;
import com.sport_pro_be.auth.forgotpassword.dto.VerifyForgotPasswordOtpRequest;

public interface IForgotPasswordService {
    String requestOtp(ForgotPasswordRequest request);
    ForgotPasswordTokenResponse verifyOtp(VerifyForgotPasswordOtpRequest request);
    String resetPassword(ResetPasswordRequest request);
}
