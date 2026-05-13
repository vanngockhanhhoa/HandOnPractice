package org.example.ash.aop;

import org.example.ash.dto.request.ThrottleAddRequest;

public interface ISignatureValidation {
    Boolean validateSignature(ThrottleAddRequest request);
}
