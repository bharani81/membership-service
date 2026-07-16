package com.membership.mapper;

import com.membership.dto.response.MembershipTierResponse;
import com.membership.entity.MembershipTier;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for {@link MembershipTier} → {@link MembershipTierResponse}.
 */
@Mapper(componentModel = "spring")
public interface MembershipTierMapper {

    MembershipTierResponse toResponse(MembershipTier tier);

    List<MembershipTierResponse> toResponseList(List<MembershipTier> tiers);
}
