package com.carlos.challenge.infrastructure.out.persistence.mongo.mapper;

import com.carlos.challenge.domain.model.Accreditation;
import com.carlos.challenge.infrastructure.out.persistence.mongo.entity.AccreditationDocument;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface AccreditationMongoMapper {

    Accreditation toDomain(AccreditationDocument doc);

    AccreditationDocument toDocument(Accreditation acc);

    default Page<Accreditation> toDomainPage(Page<AccreditationDocument> page) {
        return page.map(this::toDomain);
    }

}
