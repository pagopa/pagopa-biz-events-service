package it.gov.pagopa.bizeventsservice.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum ErrorCode {

    // biz event
    BZ_404_001,
    BZ_404_002,
    BZ_404_003,
    BZ_404_004,

    BZ_422_001,
    BZ_422_002,
    BZ_422_003,

    // generic
    GN_400_001,
    GN_400_002,
    GN_400_003,
    GN_400_004,
    GN_400_005,

    GN_500_001,
    GN_500_002,
    GN_500_003,
    GN_500_004,

    // feign client
    FG_000_001,

    // view user
    VU_404_001,
    VU_404_002,
    VU_404_003,

    // view general
    VG_404_001,

    // view cart
    VC_404_001,

    // attachment
    AT_404_001,
    AT_404_002,

    // unknown
    UN_500_000,

    // test
    TS_000_000,

}