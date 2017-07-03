The files CI360StagingAssets65.spk, CI360StagingAssetsPre65.spk and
sas.mkt.streaming.dsassets.ci360staging.jar are the delivered to the customer as part of the streaming
SDK. These three files allow the on-prem RTDM environment to stage treatments into CI360. To install
these files

1.  For each machine running the Decision Services Engine web application do the following:
          a.  Log into the machine
          b.  Copy the jars from the SDK's stage/lib into the
                 <SAS install dir>/Web/WebAppServer/SASServer7/sas_webapps/sas.decisionservices.engine.war/WEB-INF/lib folder.
          c.  Copy the SDK's stage/ci360staging.properties file into the <SAS install dir>/Web/WebAppServer/SASServer7/conf folder.
          d.  Update the properties in the copied ci360staging.properties file:
                  apiURL - https://<CI360's API Node IP Address>/api/staging
                  agentName - matches an agent created in the CI360 "Manage Access" UI.
                  tenantID - matches the tenant ID for that agent
                  clientSecret - matches the client secret for the agent
          e.  restart tcServer for SASServer7
2.  Log into SMC for the environment where the Decision Services engine is installed.
3.  Use the SMC import functionality to import the assets in CI360StagingAssetsXXX.spk.   Whether you 
          import the 65 version or the Pre65 version depends on the version of Customer Intelligence
          installed at the site.
4.  Next go to the Decision Services Manager plug-in for SMC.
5.  Drill into the "Decision Flows" folder under the Engine folder matching the engine where the asset
          were installed in step 2.
6.  Select the _SAS_ADD_STAGED_TREATMENTS_CI360_FLOW assets and use the "Activate" menu item to activate
          that flow.
7.  Any treatments that are staged by flows in this engine will now be sent to CI360.