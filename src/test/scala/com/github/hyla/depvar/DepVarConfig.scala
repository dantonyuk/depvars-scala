package com.github.hyla.depvar

import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

@Configuration
@ComponentScan
class DepVarConfig {

  @Bean
  def dependentVarContext() = new DependentVarContext
}
