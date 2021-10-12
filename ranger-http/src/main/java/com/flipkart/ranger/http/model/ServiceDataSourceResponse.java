package com.flipkart.ranger.http.model;

import com.flipkart.ranger.core.model.Service;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ServiceDataSourceResponse {
    private boolean success;
    private List<Service> services;
}
