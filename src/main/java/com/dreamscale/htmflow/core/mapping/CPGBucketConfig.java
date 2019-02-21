package com.dreamscale.htmflow.core.mapping;

public class CPGBucketConfig {

    public ProjectBuckets createBuckets() {

        ProjectBuckets projectBuckets = new ProjectBuckets();


        projectBuckets.configureBucket("About", "*/src/main/java/com/nbcuniversal/forecasting/domain/about/*");
        projectBuckets.configureBucket("About", "*/src/main/java/com/nbcuniversal/forecasting/controller/AboutController.java");

        projectBuckets.configureBucket("Accrual", "*/src/main/java/com/nbcuniversal/forecasting/domain/accrual/*");
        projectBuckets.configureBucket("Accrual", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/AccrualMapper.xml");

        projectBuckets.configureBucket("Actuals", "*/src/main/java/com/nbcuniversal/forecasting/domain/actuals/*");
        projectBuckets.configureBucket("Actuals", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ActualsMapper.xml");
        projectBuckets.configureBucket("Actuals", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ForeignExchangeMapper.xml");

        projectBuckets.configureBucket("Adjustment", "*/src/main/java/com/nbcuniversal/forecasting/domain/adjustment/*");
        projectBuckets.configureBucket("Adjustment", "*/src/main/java/com/nbcuniversal/forecasting/controller/AdjustmentsController.java");
        projectBuckets.configureBucket("Adjustment", "*/src/main/java/com/nbcuniversal/forecasting/controller/BulkEditController.java");
        projectBuckets.configureBucket("Adjustment", "*/src/main/java/com/nbcuniversal/forecasting/controller/OtherRevenueAdjustmentsController.java");
        projectBuckets.configureBucket("Adjustment", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/OtherRevenueAdjustmentMapper.xml");


        projectBuckets.configureBucket("Agent", "*/src/main/java/com/nbcuniversal/forecasting/domain/agent/*");
        projectBuckets.configureBucket("Agent", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/AgentCommissionMapper.xml");
        projectBuckets.configureBucket("Agent", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/AgentMapper.xml");

        projectBuckets.configureBucket("Asset", "*/src/main/java/com/nbcuniversal/forecasting/domain/asset/*");
        projectBuckets.configureBucket("Asset", "*/src/main/java/com/nbcuniversal/forecasting/controller/AssetController.java");
        projectBuckets.configureBucket("Asset", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/AssetDeferralPeriodMapper.xml");
        projectBuckets.configureBucket("Asset", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/AssetMapper.xml");

        projectBuckets.configureBucket("Audit", "*/src/main/java/com/nbcuniversal/forecasting/domain/audit/*");
        projectBuckets.configureBucket("Audit", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/AuditMapper.xml");

        projectBuckets.configureBucket("Channel", "*/src/main/java/com/nbcuniversal/forecasting/domain/channel/*");
        projectBuckets.configureBucket("Channel", "*/src/main/java/com/nbcuniversal/forecasting/controller/ChannelController.java");
        projectBuckets.configureBucket("Channel", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ChannelMapper.xml");

        projectBuckets.configureBucket("Cmf", "*/src/main/java/com/nbcuniversal/forecasting/domain/cmf/*");
        projectBuckets.configureBucket("Cmf", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/CmfMapper.xml");

        projectBuckets.configureBucket("Common", "*/src/main/java/com/nbcuniversal/forecasting/domain/common/*");

        projectBuckets.configureBucket("Contract", "*/src/main/java/com/nbcuniversal/forecasting/domain/contract/*");
        projectBuckets.configureBucket("Contract", "*/src/main/java/com/nbcuniversal/forecasting/controller/ContractController.java");
        projectBuckets.configureBucket("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractDetailsMapper.xml");
        projectBuckets.configureBucket("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractMapper.xml");
        projectBuckets.configureBucket("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractObjectMapper.xml");
        projectBuckets.configureBucket("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractReportDetailsMapper.xml");
        projectBuckets.configureBucket("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractSubmittedMapper.xml");
        projectBuckets.configureBucket("Contract", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/PriorQuarterMapper.xml");

        projectBuckets.configureBucket("Currency", "*/src/main/java/com/nbcuniversal/forecasting/domain/currency/*");
        projectBuckets.configureBucket("Currency", "*/src/main/java/com/nbcuniversal/forecasting/controller/CurrencyController.java");
        projectBuckets.configureBucket("Currency", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/CurrencyMapper.xml");

        projectBuckets.configureBucket("Dealmaker", "*/src/main/java/com/nbcuniversal/forecasting/domain/dealmaker/*");
        projectBuckets.configureBucket("Dealmaker", "*/src/main/java/com/nbcuniversal/forecasting/controller/DealmakerController.java");
        projectBuckets.configureBucket("Dealmaker", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/DealmakerFinanceMapper.xml");
        projectBuckets.configureBucket("Dealmaker", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/DealmakerMapper.xml");

        projectBuckets.configureBucketWithExclusions("Contract Flat Fees", "*/src/main/java/com/nbcuniversal/forecasting/domain/fees/*",
                "*/src/main/java/com/nbcuniversal/forecasting/domain/fees/gsf/*",
        "*/src/main/java/com/nbcuniversal/forecasting/domain/fees/agentcommission/*");
        projectBuckets.configureBucket("Contract Flat Fees", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractFlatFeeMapper.xml");

        projectBuckets.configureBucket("GSF", "*/src/main/java/com/nbcuniversal/forecasting/domain/fees/gsf/*");
        projectBuckets.configureBucket("GSF", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/MinimumGuaranteeBalanceMapper.xml");
        projectBuckets.configureBucket("GSF", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/RightsLogicMinimumGuaranteeBalanceMapper.xml");


        projectBuckets.configureBucket("Agent Commission", "*/src/main/java/com/nbcuniversal/forecasting/domain/fees/agentcommission/*");

        projectBuckets.configureBucket("Finance", "*/src/main/java/com/nbcuniversal/forecasting/domain/finance/*");
        projectBuckets.configureBucket("Finance", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/FinanceAdjustmentMapper.xml");

        projectBuckets.configureBucket("Forecast Details", "*/src/main/java/com/nbcuniversal/forecasting/domain/forecastdetails/*");
        projectBuckets.configureBucket("Forecast Details", "*/src/main/java/com/nbcuniversal/forecasting/controller/financeadjustment/*");

        projectBuckets.configureBucket("Forecast Report", "*/src/main/java/com/nbcuniversal/forecasting/domain/forecastreport/*");
        projectBuckets.configureBucket("Forecast Report", "*/src/main/java/com/nbcuniversal/forecasting/controller/report/*");

        projectBuckets.configureBucket("Forecast Review", "*/src/main/java/com/nbcuniversal/forecasting/domain/forecastreview/*");

        projectBuckets.configureBucket("Grouping", "*/src/main/java/com/nbcuniversal/forecasting/domain/grouping/*");

        projectBuckets.configureBucket("Invite", "*/src/main/java/com/nbcuniversal/forecasting/domain/invite/*");

        projectBuckets.configureBucket("Justification", "*/src/main/java/com/nbcuniversal/forecasting/domain/justification/*");
        projectBuckets.configureBucket("Justification", "*/src/main/java/com/nbcuniversal/forecasting/controller/JustificationController.java");
        projectBuckets.configureBucket("Justification", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/JustificationMapper.xml");

        projectBuckets.configureBucket("Licensee", "*/src/main/java/com/nbcuniversal/forecasting/domain/licensee/*");
        projectBuckets.configureBucket("Licensee", "*/src/main/java/com/nbcuniversal/forecasting/controller/LicenseeController.java");
        projectBuckets.configureBucket("Licensee", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/LicenseeMapper.xml");

        projectBuckets.configureBucket("Command", "*/src/main/java/com/nbcuniversal/forecasting/domain/command/*");

        projectBuckets.configureBucket("Prerelease", "*/src/main/java/com/nbcuniversal/forecasting/domain/prerelease/*");


        projectBuckets.configureBucket("Product", "*/src/main/java/com/nbcuniversal/forecasting/domain/product/*");
        projectBuckets.configureBucket("Product", "*/src/main/java/com/nbcuniversal/forecasting/controller/ProductController.java");
        projectBuckets.configureBucket("Product", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ProductMapper.xml");

        projectBuckets.configureBucket("Product Group", "*/src/main/java/com/nbcuniversal/forecasting/domain/productgroup/*");

        projectBuckets.configureBucket("Rate Type", "*/src/main/java/com/nbcuniversal/forecasting/domain/ratetype/*");
        projectBuckets.configureBucket("Rate Type", "*/src/main/java/com/nbcuniversal/forecasting/controller/RateTypeController.java");
        projectBuckets.configureBucket("Rate Type", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/RateTypeMapper.xml");

        projectBuckets.configureBucket("Reaccrual Report", "*/src/main/java/com/nbcuniversal/forecasting/domain/reaccrual/*");

        projectBuckets.configureBucket("Right", "*/src/main/java/com/nbcuniversal/forecasting/domain/right/*");
        projectBuckets.configureBucket("Right", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/RightLevelAdjustmentMapper.xml");
        projectBuckets.configureBucket("Right", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/RightRoyaltyRateMapper.xml");

        projectBuckets.configureBucket("Role", "*/src/main/java/com/nbcuniversal/forecasting/domain/role/*");
        projectBuckets.configureBucket("Role", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/RoleMetadataMapper.xml");

        projectBuckets.configureBucket("Royalty", "*/src/main/java/com/nbcuniversal/forecasting/domain/royalty/*");
        projectBuckets.configureBucket("Royalty", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/RoyaltyMapper.xml");
        projectBuckets.configureBucket("Royalty", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/RoyaltyStatementMapper.xml");

        projectBuckets.configureBucket("Saml", "*/src/main/java/com/nbcuniversal/forecasting/domain/saml/*");

        projectBuckets.configureBucket("Sap", "*/src/main/java/com/nbcuniversal/forecasting/domain/sap/*");
        projectBuckets.configureBucket("Sap", "*/src/main/java/com/nbcuniversal/forecasting/controller/SAPExportController.java");
        projectBuckets.configureBucket("Sap", "*/src/main/java/com/nbcuniversal/forecasting/controller/SudCalculationController.java");
        projectBuckets.configureBucket("Sap", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/SAPCalculationMapper.xml");
        projectBuckets.configureBucket("Sap", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/SAPExportAccountMapper.xml");
        projectBuckets.configureBucket("Sap", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/SAPReportContentMapper.xml");
        projectBuckets.configureBucket("Sap", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/SAPReportDetailMapper.xml");
        projectBuckets.configureBucket("Sap", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/SudRecalculationDetailsMapper.xml");

        projectBuckets.configureBucket("Term", "*/src/main/java/com/nbcuniversal/forecasting/domain/term/*");
        projectBuckets.configureBucket("Term", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/TermMapper.xml");

        projectBuckets.configureBucket("Territory", "*/src/main/java/com/nbcuniversal/forecasting/domain/territory/*");
        projectBuckets.configureBucket("Territory", "*/src/main/java/com/nbcuniversal/forecasting/controller/TerritoryController.java");
        projectBuckets.configureBucket("Territory", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/TerritoryMapper.xml");

        projectBuckets.configureBucket("User", "*/src/main/java/com/nbcuniversal/forecasting/domain/user/*");
        projectBuckets.configureBucket("User", "*/src/main/java/com/nbcuniversal/forecasting/controller/UserController.java");
        projectBuckets.configureBucket("User", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/UserMapper.xml");

        projectBuckets.configureBucket("Utils", "*/src/main/java/com/nbcuniversal/forecasting/domain/utils/*");

        projectBuckets.configureBucket("Version", "*/src/main/java/com/nbcuniversal/forecasting/domain/version/*");
        projectBuckets.configureBucket("Version", "*/src/main/java/com/nbcuniversal/forecasting/controller/ForecastVersionController.java");
        projectBuckets.configureBucket("Version", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ForecastVersionMapper.xml");

        projectBuckets.configureBucket("Cron Jobs", "*/src/main/java/com/nbcuniversal/forecasting/scheduler/*");
        projectBuckets.configureBucket("Cron Jobs", "*/src/main/java/com/nbcuniversal/forecasting/controller/CronJobController.java");

        projectBuckets.configureBucket("RSG Integration", "*/src/main/java/com/nbcuniversal/forecasting/integration/rsg/*");
        projectBuckets.configureBucket("RSG Integration", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ForecastSubmissionMapper.xml");
        projectBuckets.configureBucket("RSG Integration", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/ForecastSubmissionStatusMapper.xml");
        projectBuckets.configureBucket("RSG Integration", "*/src/main/resources/com/nbcuniversal/forecasting/mapper/LicenseeContractDurationMapper.xml");

        projectBuckets.configureBucket("GTM Integration", "*/src/main/java/com/nbcuniversal/forecasting/integration/gtm/*");
        projectBuckets.configureBucket("GTM Integration", "*/src/main/java/com/nbcuniversal/forecasting/integration/gtm/*");

        projectBuckets.configureBucket("Cache", "*/src/main/java/com/nbcuniversal/forecasting/controller/CacheController.java");


        projectBuckets.configureBucket("Code Migrations", "*/src/main/java/com/nbcuniversal/forecasting/controller/CodeMigrationController.java");
        projectBuckets.configureBucket("Authenticator", "*/src/main/java/com/nbcuniversal/forecasting/authenticator/*");
        projectBuckets.configureBucket("Authorizer", "*/src/main/java/com/nbcuniversal/forecasting/authorizer/*");


        ///src/test/java/com/nbcuniversal/forecasting/controller/UserControllerTest.java

        projectBuckets.configureBucket("Unit Tests", "*/src/test/java/com/nbcuniversal/forecasting/*Test.java");
        projectBuckets.configureBucket("Integration Tests", "*/src/test/java/com/nbcuniversal/forecasting/*IT.java");
        projectBuckets.configureBucket("Integration Tests", "*/src/test/groovy/specs/*Spec.groovy");

        projectBuckets.configureBucketWithExclusions("Test Support",
                "*/src/test/java/com/nbcuniversal/forecasting/*",
                "*/src/test/java/com/nbcuniversal/forecasting/*IT.java",
                "*/src/test/java/com/nbcuniversal/forecasting/*Test.java"
                );
        projectBuckets.configureBucket("Test Support", "*/src/test/groovy/com/nbcuniversal/forecasting/*");
        projectBuckets.configureBucket("Test Support", "*/src/test/resources/com/nbcuniversal/forecasting*");

        projectBuckets.configureBucket("Other", "*/src/main/java/com/nbcuniversal/forecasting/*");


        return projectBuckets;
    }
}
