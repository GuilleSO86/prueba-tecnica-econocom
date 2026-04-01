package com.econocom.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SsoRedirectResponse {
    
    private String redirectUrl;
    private String state;
}
