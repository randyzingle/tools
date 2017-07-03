The files CI360StagingAssets65.spk, CI360StagingAssetsPre65.spk and
sas.mkt.streaming.dsassets.ci360staging.jar are the part of the streaming SDK that
allows the on-premises RTDM environment to stage treatments into CI360. 

These file are manually created and checked into the mkt-apigw-sdk project.
To recreate this files:

1. Load the sas.mkt.streaming.dsassets project
2. Run an IFABS build on that project (the IFABS playpen must be set to use the wky/ver-m1sasds63 track)
3. The build will generate the sas.mkt.streaming.dsassets.ci360staging.jar file which can be copied
        to this project under the Source/stage/lib folder.
4. The build will also generate the CreateCI360StagingAssets utility.
5. This utility is used to create the _SAS_ADD_STAGED_TREATMENTS_CI360_ACTIVITY DS activity object and
the _SAS_ADD_STAGED_TREATMENTS_CI360_FLOW DS flow object.  Usage of the utility requires a working
Customer Intelligence 6.4 environment to build the "Pre65" version of these assets and a working
Customer Intelligence 6.5 environment to build the "65" version of these assets.
The following are the arguments for the utility:
       a. -userid <CI user ID> e.g. sasdemo
       b. -password <CI password>
       c. -logonurl URL for the SAS logon application e.g. http://ci64xmid.unx.sas.com:7980/SASLogon
       d. -designserverurl URL for DS Design server application 
                        e.g. http://ci64xmid.unx.sas.com:7980/RTDMDesign/remote/designServerWebservice
       e. -version <CI version> e.g. 6.4 or 6.5
6. Once the DS assets are created, the CI360StagingAssetsXXX.spk can be created using the SMC export
       functionality.  Then copy to .spk to the this project under the Source/stage folder.
       
Note: the CI360StagingAssetsPre65.spk works for both CI 6.3 and CI 6.4 environments
Also, there are additional jars that already were copied into the Source/stage/lib folder from the IFABS build in step 2.  These
included some jackson jars and a jjwt jar.   They shouldn't need to be re-copied since the version of those in the m1sasds63 track
shouldn't change.