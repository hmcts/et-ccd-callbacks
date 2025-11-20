	// ====== Service specific config ======
	this.dynatraceMetricType = 'java'
	this.dynatraceMetricTag = 'namespace:et'

	//Preview Config
	this.dynatraceSyntheticTestPreview = "HTTP_CHECK-04B72130DDF3F650"
	this.dynatraceDashboardIdPreview = "de880933-f603-4964-93e0-059431f6e455"
	this.dynatraceDashboardURLPreview = "https://yrk32651.live.dynatrace.com/#dashboard;gtf=-1h;gf=all;id=de880933-f603-4964-93e0-059431f6e455"
	this.dynatraceEntitySelectorPreview = "type(service),tag(\"[Kubernetes]namespace:et\"),tag(\"Environment:PREVIEW\"),entityId(\"SERVICE-B0CA8AEAD649D14A\",\"SERVICE-9B529CD4B6842930\")"
	//this.dynatraceEntitySelectorPreview = 'type(service),tag(\\"[Kubernetes]namespace:et\\"),tag(\\"Environment:PREVIEW\\"),entityName.equals(\\"PREVIEW et - et-cos \\")'

	//AAT Config
	this.dynatraceSyntheticTestAAT = "SYNTHETIC_TEST-0A0EF3314D723E08"
	this.dynatraceDashboardIdAAT = "a529a685-8c36-4e8c-8137-67de8bfcf104"
	this.dynatraceDashboardURLAAT = "https://yrk32651.live.dynatrace.com/#dashboard;gtf=-2h;gf=all;id=a529a685-8c36-4e8c-8137-67de8bfcf104"
	this.dynatraceEntitySelectorAAT = 'type(service),tag(\\"[Kubernetes]namespace:et\\"),tag(\\"Environment:AAT\\"),entityName.equals(\\"AAT et - et-cos \\")'
    
	//Perftest Config
	this.dynatraceSyntheticTest = "SYNTHETIC_TEST-56EFA62BA8AA9B13"
	this.dynatraceDashboardIdPerfTest = "a4576442-06a9-4a76-baa5-5342a525679f"
	this.dynatraceDashboardURLPerfTest = "https://yrk32651.live.dynatrace.com/#dashboard;id=a4576442-06a9-4a76-baa5-5342a525679f;applyDashboardDefaults=true"
	this.dynatraceEntitySelectorPerfTest = 'type(service),tag(\\"[Kubernetes]namespace:et\\"),tag(\\"Environment:PERF\\"),entityName.equals(\\"PERF et - et-cos \\")'

	echo "Completed Config Load..." 
	// ====== Return `this` so the caller can access it ======
	return this
