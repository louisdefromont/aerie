package org.eaa690.aerie.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

/**
 * QueuedEmail.
 */
@Entity
@Table(name = "QUEUED_EMAIL")
public class QueuedEmail extends BaseEntity implements Comparable<QueuedEmail> {
    /**
     * Email template ID.
     */
    private String templateIdKey;

    /**
     * Email Subject.
     */
    private String subjectKey;

    /**
     * Member ID.
     */
    private Long memberId;

    /**
     * Default constructor.
     */
    public QueuedEmail() {
        // Do nothing
    }

    /**
     * Constructor.
     *
     * @param templateId Email template ID
     * @param subject Email Subject
     * @param member Member ID
     */
    public QueuedEmail(final String templateId, final String subject, final Long member) {
        templateIdKey = templateId;
        subjectKey = subject;
        memberId = member;
        setCreatedAt(new Date());
        setUpdatedAt(new Date());
    }

    public String getTemplateIdKey() {
        return templateIdKey;
    }

    public void setTemplateIdKey(String templateIdKey) {
        this.templateIdKey = templateIdKey;
    }

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        this.subjectKey = subjectKey;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public int compareTo(final QueuedEmail other) {
        if (equals(other)) {
            return 0;
        }
        return 1;
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueuedEmail qe = (QueuedEmail) o;
        return Objects.equals(templateIdKey, qe.templateIdKey) &&
                Objects.equals(subjectKey, qe.subjectKey) &&
                Objects.equals(memberId, qe.memberId);
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public int hashCode() {
        return Objects.hash(templateIdKey, subjectKey, memberId);
    }

}
