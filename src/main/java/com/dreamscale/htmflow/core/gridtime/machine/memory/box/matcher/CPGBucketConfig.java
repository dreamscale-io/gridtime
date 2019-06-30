package com.dreamscale.htmflow.core.gridtime.machine.memory.box.matcher;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;

import java.util.ArrayList;
import java.util.List;

public class CPGBucketConfig {

    public List<BoxMatcherConfig> createBoxMatchers() {

        List<BoxMatcherConfig> projectBoxes = new ArrayList<>();
        
        projectBoxes.add(new BoxMatcherConfig("About", "*/src/main/java/com/nbcuniversal/forecasting/domain/about/*"));

        projectBoxes.add(new BoxMatcherConfig("About", "*/src/main/java/com/nbcuniversal/forecasting/domain/about/*"));
        projectBoxes.add(new BoxMatcherConfig("About", "*/src/main/java/com/nbcuniversal/forecasting/controller/AboutController.java"));

        projectBoxes.add(new BoxMatcherConfig("Accrual", "*/src/main/java/com/nbcuniversal/forecasting/domain/accrual/*"));
        projectBoxes.add(new BoxMatcherConfig("Accrual", "*/src/main/resources/com/nbcuniversal/forecasting/structure/AccrualMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Actuals", "*/src/main/java/com/nbcuniversal/forecasting/domain/actuals/*"));
        projectBoxes.add(new BoxMatcherConfig("Actuals", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ActualsMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Actuals", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ForeignExchangeMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Adjustment", "*/src/main/java/com/nbcuniversal/forecasting/domain/adjustment/*"));
        projectBoxes.add(new BoxMatcherConfig("Adjustment", "*/src/main/java/com/nbcuniversal/forecasting/controller/AdjustmentsController.java"));
        projectBoxes.add(new BoxMatcherConfig("Adjustment", "*/src/main/java/com/nbcuniversal/forecasting/controller/BulkEditController.java"));
        projectBoxes.add(new BoxMatcherConfig("Adjustment", "*/src/main/java/com/nbcuniversal/forecasting/controller/OtherRevenueAdjustmentsController.java"));
        projectBoxes.add(new BoxMatcherConfig("Adjustment", "*/src/main/resources/com/nbcuniversal/forecasting/structure/OtherRevenueAdjustmentMapper.xml"));


        projectBoxes.add(new BoxMatcherConfig("Agent", "*/src/main/java/com/nbcuniversal/forecasting/domain/agent/*"));
        projectBoxes.add(new BoxMatcherConfig("Agent", "*/src/main/resources/com/nbcuniversal/forecasting/structure/AgentCommissionMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Agent", "*/src/main/resources/com/nbcuniversal/forecasting/structure/AgentMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Asset", "*/src/main/java/com/nbcuniversal/forecasting/domain/asset/*"));
        projectBoxes.add(new BoxMatcherConfig("Asset", "*/src/main/java/com/nbcuniversal/forecasting/controller/AssetController.java"));
        projectBoxes.add(new BoxMatcherConfig("Asset", "*/src/main/resources/com/nbcuniversal/forecasting/structure/AssetDeferralPeriodMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Asset", "*/src/main/resources/com/nbcuniversal/forecasting/structure/AssetMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Audit", "*/src/main/java/com/nbcuniversal/forecasting/domain/audit/*"));
        projectBoxes.add(new BoxMatcherConfig("Audit", "*/src/main/resources/com/nbcuniversal/forecasting/structure/AuditMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Channel", "*/src/main/java/com/nbcuniversal/forecasting/domain/channel/*"));
        projectBoxes.add(new BoxMatcherConfig("Channel", "*/src/main/java/com/nbcuniversal/forecasting/controller/ChannelController.java"));
        projectBoxes.add(new BoxMatcherConfig("Channel", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ChannelMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Cmf", "*/src/main/java/com/nbcuniversal/forecasting/domain/cmf/*"));
        projectBoxes.add(new BoxMatcherConfig("Cmf", "*/src/main/resources/com/nbcuniversal/forecasting/structure/CmfMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Common", "*/src/main/java/com/nbcuniversal/forecasting/domain/common/*"));

        projectBoxes.add(new BoxMatcherConfig("Contract", "*/src/main/java/com/nbcuniversal/forecasting/domain/contract/*"));
        projectBoxes.add(new BoxMatcherConfig("Contract", "*/src/main/java/com/nbcuniversal/forecasting/controller/ContractController.java"));
        projectBoxes.add(new BoxMatcherConfig("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ContractDetailsMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ContractMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ContractObjectMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ContractReportDetailsMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ContractSubmittedMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/structure/PriorQuarterMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Currency", "*/src/main/java/com/nbcuniversal/forecasting/domain/currency/*"));
        projectBoxes.add(new BoxMatcherConfig("Currency", "*/src/main/java/com/nbcuniversal/forecasting/controller/CurrencyController.java"));
        projectBoxes.add(new BoxMatcherConfig("Currency", "*/src/main/resources/com/nbcuniversal/forecasting/structure/CurrencyMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Dealmaker", "*/src/main/java/com/nbcuniversal/forecasting/domain/dealmaker/*"));
        projectBoxes.add(new BoxMatcherConfig("Dealmaker", "*/src/main/java/com/nbcuniversal/forecasting/controller/DealmakerController.java"));
        projectBoxes.add(new BoxMatcherConfig("Dealmaker", "*/src/main/resources/com/nbcuniversal/forecasting/structure/DealmakerFinanceMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Dealmaker", "*/src/main/resources/com/nbcuniversal/forecasting/structure/DealmakerMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Contract Flat Fees", "*/src/main/java/com/nbcuniversal/forecasting/domain/fees/*",
                DefaultCollections.toList("*/src/main/java/com/nbcuniversal/forecasting/domain/fees/gsf/*",
                                                  "*/src/main/java/com/nbcuniversal/forecasting/domain/fees/agentcommission/*")));
        projectBoxes.add(new BoxMatcherConfig("Contract Flat Fees", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ContractFlatFeeMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("GSF", "*/src/main/java/com/nbcuniversal/forecasting/domain/fees/gsf/*"));
        projectBoxes.add(new BoxMatcherConfig("GSF", "*/src/main/resources/com/nbcuniversal/forecasting/structure/MinimumGuaranteeBalanceMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("GSF", "*/src/main/resources/com/nbcuniversal/forecasting/structure/RightsLogicMinimumGuaranteeBalanceMapper.xml"));


        projectBoxes.add(new BoxMatcherConfig("Agent Commission", "*/src/main/java/com/nbcuniversal/forecasting/domain/fees/agentcommission/*"));

        projectBoxes.add(new BoxMatcherConfig("Finance", "*/src/main/java/com/nbcuniversal/forecasting/domain/finance/*"));
        projectBoxes.add(new BoxMatcherConfig("Finance", "*/src/main/resources/com/nbcuniversal/forecasting/structure/FinanceAdjustmentMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Forecast Details", "*/src/main/java/com/nbcuniversal/forecasting/domain/forecastdetails/*"));
        projectBoxes.add(new BoxMatcherConfig("Forecast Details", "*/src/main/java/com/nbcuniversal/forecasting/controller/financeadjustment/*"));

        projectBoxes.add(new BoxMatcherConfig("Forecast Report", "*/src/main/java/com/nbcuniversal/forecasting/domain/forecastreport/*"));
        projectBoxes.add(new BoxMatcherConfig("Forecast Report", "*/src/main/java/com/nbcuniversal/forecasting/controller/report/*"));

        projectBoxes.add(new BoxMatcherConfig("Forecast Review", "*/src/main/java/com/nbcuniversal/forecasting/domain/forecastreview/*"));

        projectBoxes.add(new BoxMatcherConfig("Grouping", "*/src/main/java/com/nbcuniversal/forecasting/domain/grouping/*"));

        projectBoxes.add(new BoxMatcherConfig("Invite", "*/src/main/java/com/nbcuniversal/forecasting/domain/invite/*"));

        projectBoxes.add(new BoxMatcherConfig("Justification", "*/src/main/java/com/nbcuniversal/forecasting/domain/justification/*"));
        projectBoxes.add(new BoxMatcherConfig("Justification", "*/src/main/java/com/nbcuniversal/forecasting/controller/JustificationController.java"));
        projectBoxes.add(new BoxMatcherConfig("Justification", "*/src/main/resources/com/nbcuniversal/forecasting/structure/JustificationMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Licensee", "*/src/main/java/com/nbcuniversal/forecasting/domain/licensee/*"));
        projectBoxes.add(new BoxMatcherConfig("Licensee", "*/src/main/java/com/nbcuniversal/forecasting/controller/LicenseeController.java"));
        projectBoxes.add(new BoxMatcherConfig("Licensee", "*/src/main/resources/com/nbcuniversal/forecasting/structure/LicenseeMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Command", "*/src/main/java/com/nbcuniversal/forecasting/domain/command/*"));

        projectBoxes.add(new BoxMatcherConfig("Prerelease", "*/src/main/java/com/nbcuniversal/forecasting/domain/prerelease/*"));


        projectBoxes.add(new BoxMatcherConfig("Product", "*/src/main/java/com/nbcuniversal/forecasting/domain/product/*"));
        projectBoxes.add(new BoxMatcherConfig("Product", "*/src/main/java/com/nbcuniversal/forecasting/controller/ProductController.java"));
        projectBoxes.add(new BoxMatcherConfig("Product", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ProductMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Product Group", "*/src/main/java/com/nbcuniversal/forecasting/domain/productgroup/*"));

        projectBoxes.add(new BoxMatcherConfig("Rate Type", "*/src/main/java/com/nbcuniversal/forecasting/domain/ratetype/*"));
        projectBoxes.add(new BoxMatcherConfig("Rate Type", "*/src/main/java/com/nbcuniversal/forecasting/controller/RateTypeController.java"));
        projectBoxes.add(new BoxMatcherConfig("Rate Type", "*/src/main/resources/com/nbcuniversal/forecasting/structure/RateTypeMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Reaccrual Report", "*/src/main/java/com/nbcuniversal/forecasting/domain/reaccrual/*"));

        projectBoxes.add(new BoxMatcherConfig("Right", "*/src/main/java/com/nbcuniversal/forecasting/domain/right/*"));
        projectBoxes.add(new BoxMatcherConfig("Right", "*/src/main/resources/com/nbcuniversal/forecasting/structure/RightLevelAdjustmentMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Right", "*/src/main/resources/com/nbcuniversal/forecasting/structure/RightRoyaltyRateMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Role", "*/src/main/java/com/nbcuniversal/forecasting/domain/role/*"));
        projectBoxes.add(new BoxMatcherConfig("Role", "*/src/main/resources/com/nbcuniversal/forecasting/structure/RoleMetadataMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Royalty", "*/src/main/java/com/nbcuniversal/forecasting/domain/royalty/*"));
        projectBoxes.add(new BoxMatcherConfig("Royalty", "*/src/main/resources/com/nbcuniversal/forecasting/structure/RoyaltyMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Royalty", "*/src/main/resources/com/nbcuniversal/forecasting/structure/RoyaltyStatementMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Saml", "*/src/main/java/com/nbcuniversal/forecasting/domain/saml/*"));

        projectBoxes.add(new BoxMatcherConfig("Sap", "*/src/main/java/com/nbcuniversal/forecasting/domain/sap/*"));
        projectBoxes.add(new BoxMatcherConfig("Sap", "*/src/main/java/com/nbcuniversal/forecasting/controller/SAPExportController.java"));
        projectBoxes.add(new BoxMatcherConfig("Sap", "*/src/main/java/com/nbcuniversal/forecasting/controller/SudCalculationController.java"));
        projectBoxes.add(new BoxMatcherConfig("Sap", "*/src/main/resources/com/nbcuniversal/forecasting/structure/SAPCalculationMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Sap", "*/src/main/resources/com/nbcuniversal/forecasting/structure/SAPExportAccountMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Sap", "*/src/main/resources/com/nbcuniversal/forecasting/structure/SAPReportContentMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Sap", "*/src/main/resources/com/nbcuniversal/forecasting/structure/SAPReportDetailMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("Sap", "*/src/main/resources/com/nbcuniversal/forecasting/structure/SudRecalculationDetailsMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Term", "*/src/main/java/com/nbcuniversal/forecasting/domain/term/*"));
        projectBoxes.add(new BoxMatcherConfig("Term", "*/src/main/resources/com/nbcuniversal/forecasting/structure/TermMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Territory", "*/src/main/java/com/nbcuniversal/forecasting/domain/territory/*"));
        projectBoxes.add(new BoxMatcherConfig("Territory", "*/src/main/java/com/nbcuniversal/forecasting/controller/TerritoryController.java"));
        projectBoxes.add(new BoxMatcherConfig("Territory", "*/src/main/resources/com/nbcuniversal/forecasting/structure/TerritoryMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("User", "*/src/main/java/com/nbcuniversal/forecasting/domain/user/*"));
        projectBoxes.add(new BoxMatcherConfig("User", "*/src/main/java/com/nbcuniversal/forecasting/controller/UserController.java"));
        projectBoxes.add(new BoxMatcherConfig("User", "*/src/main/resources/com/nbcuniversal/forecasting/structure/UserMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Utils", "*/src/main/java/com/nbcuniversal/forecasting/domain/utils/*"));

        projectBoxes.add(new BoxMatcherConfig("Version", "*/src/main/java/com/nbcuniversal/forecasting/domain/version/*"));
        projectBoxes.add(new BoxMatcherConfig("Version", "*/src/main/java/com/nbcuniversal/forecasting/controller/ForecastVersionController.java"));
        projectBoxes.add(new BoxMatcherConfig("Version", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ForecastVersionMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("Cron Jobs", "*/src/main/java/com/nbcuniversal/forecasting/scheduler/*"));
        projectBoxes.add(new BoxMatcherConfig("Cron Jobs", "*/src/main/java/com/nbcuniversal/forecasting/controller/CronJobController.java"));

        projectBoxes.add(new BoxMatcherConfig("RSG Integration", "*/src/main/java/com/nbcuniversal/forecasting/integration/rsg/*"));
        projectBoxes.add(new BoxMatcherConfig("RSG Integration", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ForecastSubmissionMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("RSG Integration", "*/src/main/resources/com/nbcuniversal/forecasting/structure/ForecastSubmissionStatusMapper.xml"));
        projectBoxes.add(new BoxMatcherConfig("RSG Integration", "*/src/main/resources/com/nbcuniversal/forecasting/structure/LicenseeContractDurationMapper.xml"));

        projectBoxes.add(new BoxMatcherConfig("GTM Integration", "*/src/main/java/com/nbcuniversal/forecasting/integration/gtm/*"));
        projectBoxes.add(new BoxMatcherConfig("GTM Integration", "*/src/main/java/com/nbcuniversal/forecasting/integration/gtm/*"));

        projectBoxes.add(new BoxMatcherConfig("Cache", "*/src/main/java/com/nbcuniversal/forecasting/controller/CacheController.java"));


        projectBoxes.add(new BoxMatcherConfig("Code Migrations", "*/src/main/java/com/nbcuniversal/forecasting/controller/CodeMigrationController.java"));
        projectBoxes.add(new BoxMatcherConfig("Authenticator", "*/src/main/java/com/nbcuniversal/forecasting/authenticator/*"));
        projectBoxes.add(new BoxMatcherConfig("Authorizer", "*/src/main/java/com/nbcuniversal/forecasting/authorizer/*"));


        ///src/test/java/com/nbcuniversal/forecasting/controller/UserControllerTest.java

        projectBoxes.add(new BoxMatcherConfig("Unit Tests", "*/src/test/java/com/nbcuniversal/forecasting/*Test.java"));
        projectBoxes.add(new BoxMatcherConfig("Integration Tests", "*/src/test/java/com/nbcuniversal/forecasting/*IT.java"));
        projectBoxes.add(new BoxMatcherConfig("Integration Tests", "*/src/test/groovy/specs/*Spec.groovy"));

        projectBoxes.add(new BoxMatcherConfig("Test Support",
                "*/src/test/java/com/nbcuniversal/forecasting/*",
                DefaultCollections.toList(
                "*/src/test/java/com/nbcuniversal/forecasting/*IT.java",
                "*/src/test/java/com/nbcuniversal/forecasting/*Test.java"
                )));
        projectBoxes.add(new BoxMatcherConfig("Test Support", "*/src/test/groovy/com/nbcuniversal/forecasting/*"));
        projectBoxes.add(new BoxMatcherConfig("Test Support", "*/src/test/resources/com/nbcuniversal/forecasting*"));

        projectBoxes.add(new BoxMatcherConfig("Other", "*/src/main/java/com/nbcuniversal/forecasting/*"));


        return projectBoxes;
    }


}
