package com.buildingaccess.service;

import com.buildingaccess.dto.apartment.ApartmentRequest;
import com.buildingaccess.dto.apartment.ApartmentResponse;
import com.buildingaccess.model.Apartment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApartmentService {

    /** Puna (nepaginirana) lista — koristi je forma za registraciju stanara (SK2), gde je potreban ceo izbor. */
    List<ApartmentResponse> getByBuilding(Long buildingId);

    /** Paginirana lista za admin CRUD prikaz (SK14). */
    Page<ApartmentResponse> search(Long buildingId, Pageable pageable);

    ApartmentResponse getById(Long id);

    /** Javno jer je poziva i UserService. */
    Apartment findEntity(Long id);

    ApartmentResponse create(ApartmentRequest request);

    ApartmentResponse update(Long id, ApartmentRequest request);

    void delete(Long id);
}
