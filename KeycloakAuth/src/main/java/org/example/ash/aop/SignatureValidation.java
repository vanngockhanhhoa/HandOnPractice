package org.example.ash.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ash.dto.request.ThrottleAddRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@RequiredArgsConstructor
@Component("SignatureValidation")
public class SignatureValidation {


    public Boolean validateSignature(ThrottleAddRequest request) {
        switch (request.getScopeType()) {
            case "user":
//                if (CollectionUtils.isEmpty(request.getUserName())) throw new Exception();
                return true;
            case "app":
                return true;
            default:
                return false;
        }
    }
}
