package com.carlos.challenge.infrastructure.in.web.mapper;

import com.carlos.challenge.domain.model.Accreditation;
import com.carlos.challenge.infrastructure.in.web.dto.resp.AccreditationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccreditationWebMapper {
    AccreditationResponse toResponse(Accreditation acc);
}