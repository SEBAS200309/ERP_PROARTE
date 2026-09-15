package com.proarte.erp.roles.service;

import com.proarte.erp.roles.dto.CreateRolRequest;
import com.proarte.erp.roles.dto.RolResponse;
import com.proarte.erp.roles.dto.UpdateRolRequest;

import java.util.List;
import java.util.UUID;

public interface RolService {

    List<RolResponse> getAll();

    RolResponse getById(UUID id);

    RolResponse create(CreateRolRequest request);

    RolResponse update(UUID id, UpdateRolRequest request);

    void delete(UUID id);
}
