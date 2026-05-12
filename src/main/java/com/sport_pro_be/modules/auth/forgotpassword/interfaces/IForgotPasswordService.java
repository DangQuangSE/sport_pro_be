package com.sport_pro_be.modules.auth.forgotpassword.interfaces;

import com.sport_pro_be.modules.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.sport_pro_be.modules.auth.forgotpassword.dto.ForgotPasswordTokenResponse;
import com.sport_pro_be.modules.auth.forgotpassword.dto.ResetPasswordRequest;
import com.sport_pro_be.modules.auth.forgotpassword.dto.VerifyForgotPasswordOtpRequest;

public interface IForgotPasswordService {
    String requestOtp(ForgotPasswordRequest request);
    ForgotPasswordTokenResponse verifyOtp(VerifyForgotPasswordOtpRequest request);
    String resetPassword(ResetPasswordRequest request);
}

