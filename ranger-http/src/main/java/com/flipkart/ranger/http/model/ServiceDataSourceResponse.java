package com.flipkart.ranger.http.model;

import com.flipkart.ranger.core.model.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceDataSourceResponse {
    private boolean success;
    private List<Service> services;
}
