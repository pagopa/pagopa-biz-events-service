package it.gov.pagopa.bizeventsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebtorPosition {
	private String modelType;
	private String noticeNumber;
	private String iuv;
	private String iur;
}
