package com.midlane.project_management_tool_user_service.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMembershipId implements Serializable {

    private Long userId;
    private Long organizationId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationMembershipId that = (OrganizationMembershipId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, organizationId);
    }
}
