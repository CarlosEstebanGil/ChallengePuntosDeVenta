package com.carlos.challenge.infrastructure.in.web.mapper;

import com.carlos.challenge.domain.model.PointOfSale;
import com.carlos.challenge.infrastructure.in.web.dto.resp.PointOfSaleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PointOfSaleWebMapper {

    PointOfSaleResponse toResponse(PointOfSale pos);

}
