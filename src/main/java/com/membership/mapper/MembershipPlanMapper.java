package com.membership.mapper;

import com.membership.dto.response.MembershipPlanResponse;
import com.membership.entity.MembershipPlan;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for {@link MembershipPlan} → {@link MembershipPlanResponse}.
 * Zero manual field assignment — all mapping is generated at compile time.
 */
@Mapper(componentModel = "spring")
public interface MembershipPlanMapper {

    MembershipPlanResponse toResponse(MembershipPlan plan);

    List<MembershipPlanResponse> toResponseList(List<MembershipPlan> plans);
}
