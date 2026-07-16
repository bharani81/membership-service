package com.membership.mapper;

import com.membership.dto.response.UserMembershipResponse;
import com.membership.entity.UserMembership;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for {@link UserMembership} → {@link UserMembershipResponse}.
 * Uses the plan and tier mappers to produce nested response objects.
 */
@Mapper(componentModel = "spring", uses = {MembershipPlanMapper.class, MembershipTierMapper.class})
public interface UserMembershipMapper {

    UserMembershipResponse toResponse(UserMembership membership);
}
