package com.dreamscale.htmflow.core.mapping;

public class CPGBucketConfig {

    public ProjectBuckets createBuckets() {

        ProjectBuckets projectBuckets = new ProjectBuckets();

        projectBuckets.configureBucket("About", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/about/*");
        projectBuckets.configureBucket("About", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/AboutController.java");

        projectBuckets.configureBucket("Accrual", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/accrual/*");
        projectBuckets.configureBucket("Accrual", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/AccrualMapper.xml");

        projectBuckets.configureBucket("Actuals", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/actuals/*");
        projectBuckets.configureBucket("Actuals", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ActualsMapper.xml");
        projectBuckets.configureBucket("Actuals", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ForeignExchangeMapper.xml");

        projectBuckets.configureBucket("Adjustment", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/adjustment/*");
        projectBuckets.configureBucket("Adjustment", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/AdjustmentsController");
        projectBuckets.configureBucket("Adjustment", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/BulkEditController");
        projectBuckets.configureBucket("Adjustment", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/OtherRevenueAdjustmentsController");
        projectBuckets.configureBucket("Adjustment", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/OtherRevenueAdjustmentMapper.xml");


        projectBuckets.configureBucket("Agent", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/agent/*");
        projectBuckets.configureBucket("Agent", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/AgentCommissionMapper.xml");
        projectBuckets.configureBucket("Agent", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/AgentMapper.xml");

        projectBuckets.configureBucket("Asset", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/asset/*");
        projectBuckets.configureBucket("Asset", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/AssetController");
        projectBuckets.configureBucket("Asset", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/AssetDeferralPeriodMapper.xml");
        projectBuckets.configureBucket("Asset", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/AssetMapper.xml");

        projectBuckets.configureBucket("Audit", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/audit/*");
        projectBuckets.configureBucket("Audit", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/AuditMapper.xml");

        projectBuckets.configureBucket("Channel", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/channel/*");
        projectBuckets.configureBucket("Channel", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/ChannelController");
        projectBuckets.configureBucket("Channel", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ChannelMapper.xml");

        projectBuckets.configureBucket("Cmf", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/cmf/*");
        projectBuckets.configureBucket("Cmf", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/CmfMapper.xml");

        projectBuckets.configureBucket("Common", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/common/*");

        projectBuckets.configureBucket("Contract", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/contract/*");
        projectBuckets.configureBucket("Contract", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/ContractController");
        projectBuckets.configureBucket("Contract", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractDetailsMapper.xml");
        projectBuckets.configureBucket("Contract", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractMapper.xml");
        projectBuckets.configureBucket("Contract", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractObjectMapper.xml");
        projectBuckets.configureBucket("Contract", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractReportDetailsMapper.xml");
        projectBuckets.configureBucket("Contract", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractSubmittedMapper.xml");
        projectBuckets.configureBucket("Contract", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/PriorQuarterMapper.xml");

        projectBuckets.configureBucket("Currency", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/currency/*");
        projectBuckets.configureBucket("Currency", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/CurrencyController");
        projectBuckets.configureBucket("Currency", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/CurrencyMapper.xml");

        projectBuckets.configureBucket("Dealmaker", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/dealmaker/*");
        projectBuckets.configureBucket("Dealmaker", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/DealmakerController");
        projectBuckets.configureBucket("Dealmaker", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/DealmakerFinanceMapper.xml");
        projectBuckets.configureBucket("Dealmaker", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/DealmakerMapper.xml");

        projectBuckets.configureBucketWithExclusions("Contract Flat Fees", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/fees/*",
                "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/fees/gsf/*",
        "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/fees/agentcommission/*");
        projectBuckets.configureBucket("Contract Flat Fees", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ContractFlatFeeMapper.xml");

        projectBuckets.configureBucket("GSF", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/fees/gsf/*");
        projectBuckets.configureBucket("GSF", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/MinimumGuaranteeBalanceMapper.xml");
        projectBuckets.configureBucket("GSF", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/RightsLogicMinimumGuaranteeBalanceMapper.xml");


        projectBuckets.configureBucket("Agent Commission", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/fees/agentcommission/*");

        projectBuckets.configureBucket("Finance", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/finance/*");
        projectBuckets.configureBucket("Finance", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/FinanceAdjustmentMapper.xml");

        projectBuckets.configureBucket("Forecast Details", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/forecastdetails/*");
        projectBuckets.configureBucket("Forecast Details", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/financeadjustment/*");

        projectBuckets.configureBucket("Forecast Report", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/forecastreport/*");
        projectBuckets.configureBucket("Forecast Report", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/report/*");

        projectBuckets.configureBucket("Forecast Review", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/forecastreview/*");

        projectBuckets.configureBucket("Grouping", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/grouping/*");

        projectBuckets.configureBucket("Invite", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/invite/*");

        projectBuckets.configureBucket("Justification", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/justification/*");
        projectBuckets.configureBucket("Justification", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/JustificationController");
        projectBuckets.configureBucket("Justification", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/JustificationMapper.xml");

        projectBuckets.configureBucket("Licensee", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/licensee/*");
        projectBuckets.configureBucket("Licensee", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/LicenseeController");
        projectBuckets.configureBucket("Licensee", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/LicenseeMapper.xml");

        projectBuckets.configureBucket("Command", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/command/*");

        projectBuckets.configureBucket("Prerelease", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/prerelease/*");


        projectBuckets.configureBucket("Product", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/product/*");
        projectBuckets.configureBucket("Product", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/ProductController");
        projectBuckets.configureBucket("Product", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ProductMapper.xml");

        projectBuckets.configureBucket("Product Group", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/productgroup/*");

        projectBuckets.configureBucket("Rate Type", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/ratetype/*");
        projectBuckets.configureBucket("Rate Type", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/RateTypeController");
        projectBuckets.configureBucket("Rate Type", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/RateTypeMapper.xml");

        projectBuckets.configureBucket("Reaccrual Report", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/reaccrual/*");

        projectBuckets.configureBucket("Right", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/right/*");
        projectBuckets.configureBucket("Right", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/RightLevelAdjustmentMapper.xml");
        projectBuckets.configureBucket("Right", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/RightRoyaltyRateMapper.xml");

        projectBuckets.configureBucket("Role", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/role/*");
        projectBuckets.configureBucket("Role", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/RoleMetadataMapper.xml");

        projectBuckets.configureBucket("Royalty", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/royalty/*");
        projectBuckets.configureBucket("Royalty", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/RoyaltyMapper.xml");
        projectBuckets.configureBucket("Royalty", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/RoyaltyStatementMapper.xml");

        projectBuckets.configureBucket("Saml", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/saml/*");

        projectBuckets.configureBucket("Sap", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/sap/*");
        projectBuckets.configureBucket("Sap", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/SAPExportController");
        projectBuckets.configureBucket("Sap", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/SudCalculationController");
        projectBuckets.configureBucket("Sap", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/SAPCalculationMapper.xml");
        projectBuckets.configureBucket("Sap", "/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/SAPExportAccountMapper.xml");
        projectBuckets.configureBucket("Sap", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/SAPReportContentMapper.xml");
        projectBuckets.configureBucket("Sap", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/SAPReportDetailMapper.xml");
        projectBuckets.configureBucket("Sap", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/SudRecalculationDetailsMapper.xml");

        projectBuckets.configureBucket("Term", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/term/*");
        projectBuckets.configureBucket("Term", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/TermMapper.xml");

        projectBuckets.configureBucket("Territory", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/territory/*");
        projectBuckets.configureBucket("Territory", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/TerritoryController");
        projectBuckets.configureBucket("Territory", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/TerritoryMapper.xml");

        projectBuckets.configureBucket("User", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/user/*");
        projectBuckets.configureBucket("User", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/UserController");
        projectBuckets.configureBucket("User", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/UserMapper.xml");

        projectBuckets.configureBucket("Utils", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/utils/*");

        projectBuckets.configureBucket("Version", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/domain/version/*");
        projectBuckets.configureBucket("Version", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/ForecastVersionController");
        projectBuckets.configureBucket("Version", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ForecastVersionMapper.xml");

        projectBuckets.configureBucket("Cron Jobs", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/scheduler/*");
        projectBuckets.configureBucket("Cron Jobs", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/CronJobController");

        projectBuckets.configureBucket("RSG Integration", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/integration/rsg/*");
        projectBuckets.configureBucket("RSG Integration", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ForecastSubmissionMapper.xml");
        projectBuckets.configureBucket("RSG Integration", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/ForecastSubmissionStatusMapper.xml");
        projectBuckets.configureBucket("RSG Integration", "/forecasting/project/forecasting_backend/src/main/resources/com/nbcuniversal/forecasting/mapper/LicenseeContractDurationMapper.xml");

        projectBuckets.configureBucket("GTM Integration", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/integration/gtm/*");
        projectBuckets.configureBucket("GTM Integration", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/integration/gtm/*");

        projectBuckets.configureBucket("Cache", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/CacheController");


        projectBuckets.configureBucket("Code Migrations", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/controller/CodeMigrationController");
        projectBuckets.configureBucket("Authenticator", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/authenticator/*");
        projectBuckets.configureBucket("Authorizer", "/forecasting/project/forecasting_backend/src/main/java/com/nbcuniversal/forecasting/authorizer/*");

        projectBuckets.configureBucket("Unit Tests", "/forecasting/project/forecasting_backend/src/test/java/com/nbcuniversal/forecasting/*Test.java");
        projectBuckets.configureBucket("Integration Tests", "/forecasting/project/forecasting_backend/src/test/java/com/nbcuniversal/forecasting/*IT.java");
        projectBuckets.configureBucket("Integration Tests", "/forecasting/project/forecasting_backend/src/test/groovy/specs/*Spec.groovy");

        projectBuckets.configureBucketWithExclusions("Test Support",
                "/forecasting/project/forecasting_backend/src/test/java/com/nbcuniversal/forecasting/*",
                "/forecasting/project/forecasting_backend/src/test/java/com/nbcuniversal/forecasting/*IT.java",
                "/forecasting/project/forecasting_backend/src/test/java/com/nbcuniversal/forecasting/*Test.java"
                );
        projectBuckets.configureBucket("Test Support", "/forecasting/project/forecasting_backend/src/test/groovy/com/nbcuniversal/forecasting/*");

        return projectBuckets;
    }
}
