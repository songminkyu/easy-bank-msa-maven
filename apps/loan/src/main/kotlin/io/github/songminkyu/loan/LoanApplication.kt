package io.github.songminkyu.loan

import io.github.songminkyu.loan.dto.LoanContactInfoDTO
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@EnableConfigurationProperties(value = [LoanContactInfoDTO::class])
@SpringBootApplication
class LoanApplication
fun main(args: Array<String>) {
    runApplication<LoanApplication>(*args)
}
