package it.gov.pagopa.bizeventsservice.config;


import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.mapper.ConvertBizEventEntityToCtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;

@Configuration
public class MappingsConfiguration {

    @Bean
    ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        
        Converter<BizEvent, CtReceiptModelResponse> convertBizEventEntityToCtReceiptModelResponse = new ConvertBizEventEntityToCtReceiptModelResponse();
        mapper.createTypeMap(BizEvent.class, CtReceiptModelResponse.class).setConverter(convertBizEventEntityToCtReceiptModelResponse);
        
        return mapper;
    }

}
