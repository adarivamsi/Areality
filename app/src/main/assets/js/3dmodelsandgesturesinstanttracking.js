var defaultScaleValue = 0.045;
var defaultRotationValue = 0;

var rotationValues = [];
var scaleValues = [];

var allCurrentModels = [];
var allModelImgSources = ["assets/buttons/clock.png","assets/buttons/couch.png","assets/buttons/office_chair.png","assets/buttons/table.png","assets/buttons/trainer.png"] ;

var deleteObjBtnPos;

var oneFingerGestureAllowed = false;

// this global callback can be utilized to react on the transition from and to 2
// finger gestures; specifically, we disallow the drag gesture in this case to ensure an
// intuitive experience
AR.context.on2FingerGestureStarted = function() {
    oneFingerGestureAllowed = false;
};

var World = {
    modelPaths: ["assets/models/clock.wt3", "assets/models/couch.wt3", "assets/models/officechair.wt3", "assets/models/table.wt3", "assets/models/trainer.wt3" ],
    /*
        requestedModel is the index of the next model to be created. This is necessary because we have to wait one frame in order to pass the correct initial position to the newly created model.
        initialDrag is a boolean that serves the purpose of swiping the model into the scene. In the moment that the model is created, the drag event has already started and will not be caught by the model, so the motion has to be carried out by the tracking plane.
        lastAddedModel always holds the newest model in allCurrentModels so that the plane knows which model to apply the motion to.
    */
    requestedModel: -1,
    initialDrag: false,
    lastAddedModel: null,

/*    init: function initFn() {
        $("#inputs").empty();
        for(var i=0;i<allModelImgSources.length;i++){
            $("#inputs").append("<input data-id=" + i + " class='tracking-model-button list-group-item' type='image' src="+allModelImgSources[i]+" />");
        }
        $("#inputs").append("<input id='tracking-model-reset-button' class='tracking-model-button list-group-item' type='image' src='assets/buttons/trash.png' onclick='World.resetModels()' />");
        this.createOverlays();
    },*/

    createOverlays: function createOverlaysFn() {
        var crossHairsRedImage = new AR.ImageResource("assets/crosshairs_red.png");
        var crossHairsRedDrawable = new AR.ImageDrawable(crossHairsRedImage, 1.0);

        var crossHairsBlueImage = new AR.ImageResource("assets/crosshairs_blue.png");
        var crossHairsBlueDrawable = new AR.ImageDrawable(crossHairsBlueImage, 1.0);

        this.tracker = new AR.InstantTracker({
            onChangedState:  function onChangedStateFn(state) {
                // react to a change in tracking state here
            },
            deviceHeight: 1.0,
            onError: function(errorMessage) {
                alert(errorMessage);
            }
        });

        this.instantTrackable = new AR.InstantTrackable(this.tracker, {
            drawables: {
                cam: crossHairsBlueDrawable,
                initialization: crossHairsRedDrawable
            },
            onTrackingStarted: function onTrackingStartedFn() {
                // do something when tracking is started (recognized)
            },
            onTrackingStopped: function onTrackingStoppedFn() {
                // do something when tracking is stopped (lost)
            },
            onTrackingPlaneClick: function onTrackingPlaneClickFn(xPos, yPos) {
                // react to a the tracking plane being clicked here
            },
            onTrackingPlaneDragBegan: function onTrackingPlaneDragBeganFn(xPos, yPos) {
                oneFingerGestureAllowed = true;
                World.updatePlaneDrag(xPos, yPos);
            },
            onTrackingPlaneDragChanged: function onTrackingPlaneDragChangedFn(xPos, yPos) {
                World.updatePlaneDrag(xPos, yPos);
            },
            onTrackingPlaneDragEnded: function onTrackingPlaneDragEndedFn(xPos, yPos) {
                World.updatePlaneDrag(xPos, yPos);
                World.initialDrag = false;
            },
            onError: function(errorMessage) {
                alert(errorMessage);
            }
        });

        World.setupEventListeners()
    },

    setupEventListeners: function setupEventListenersFn() {

            $('.tracking-model-button-inactive').on('click',function(){
                World.requestedModel = $(this).data("id");
            });
    },

    updatePlaneDrag: function updatePlaneDragFn(xPos, yPos) {
        if (World.requestedModel >= 0) {
            World.addModel(World.requestedModel, xPos, yPos);
            World.requestedModel = -1;
            World.initialDrag = true;
        }

        if (World.initialDrag && oneFingerGestureAllowed) {
            lastAddedModel.translate = {x:xPos, y:yPos};
        }
    },

    changeTrackerState: function changeTrackerStateFn() {

        if (this.tracker.state === AR.InstantTrackerState.INITIALIZING) {

            $("#sidebar").show();
            document.getElementById("tracking-start-stop-button").src = "assets/buttons/stop.png";
            document.getElementById("tracking-height-slider-container").style.visibility = "hidden";

            this.tracker.state = AR.InstantTrackerState.TRACKING;
        } else {

            $("#sidebar").hide();
            document.getElementById("tracking-start-stop-button").src = "assets/buttons/start.png";
            document.getElementById("tracking-height-slider-container").style.visibility = "visible";

            this.tracker.state = AR.InstantTrackerState.INITIALIZING;
        }
    },

    changeTrackingHeight: function changeTrackingHeightFn(height) {
        this.tracker.deviceHeight = parseFloat(height);
    },

    addModel: function addModelFn(pathIndex, xpos, ypos) {
        if (World.isTracking()) {
            var modelIndex = rotationValues.length;
            World.addModelValues();

            var model = new AR.Model(World.modelPaths[pathIndex], {
                scale: {
                    x: defaultScaleValue,
                    y: defaultScaleValue,
                    z: defaultScaleValue
                },
                translate: {
                    x: xpos,
                    y: ypos
                },
                // We recommend only implementing the callbacks actually needed as they will
                // cause calls from native to JavaScript being invoked. Especially for the
                // frequently called changed callbacks this should be avoided. In this
                // sample all callbacks are implemented simply for demonstrative purposes.
                onDragBegan: function(x, y) {
                    oneFingerGestureAllowed = true;
                },
                onDragChanged: function(relativeX, relativeY, intersectionX, intersectionY) {
                    if (oneFingerGestureAllowed) {
                        // We recommend setting the entire translate property rather than
                        // its individual components as the latter would cause several
                        // call to native, which can potentially lead to performance
                        // issues on older devices. The same applied to the rotate and
                        // scale property
                        this.translate = {x:intersectionX, y:intersectionY};
                    }
                },
                onDragEnded: function(x, y) {
                    // react to the drag gesture ending
                },
                onRotationBegan: function(angleInDegrees) {
                    // react to the rotation gesture beginning
                },
                onRotationChanged: function(angleInDegrees) {
                    this.rotate.z = rotationValues[modelIndex] - angleInDegrees;
                },
                onRotationEnded: function(angleInDegrees) {
                   rotationValues[modelIndex] = this.rotate.z
                },
                onScaleBegan: function(scale) {
                    // react to the scale gesture beginning
                },
                onScaleChanged: function(scale) {
                    var scaleValue = scaleValues[modelIndex] * scale;
                    this.scale = {x: scaleValue, y: scaleValue, z: scaleValue};
                },
                onScaleEnded: function(scale) {
                    scaleValues[modelIndex] = this.scale.x;
                }
            });

            allCurrentModels.push(model);
            lastAddedModel = model;
            this.instantTrackable.drawables.addCamDrawable(model);
        }
    },

    isTracking: function isTrackingFn() {
        return (this.tracker.state === AR.InstantTrackerState.TRACKING);
    },

    addModelValues: function addModelValuesFn() {
        rotationValues.push(defaultRotationValue);
        scaleValues.push(defaultScaleValue);
    },

    resetModels: function resetModelsFn() {
    if (confirm('Are you sure you want to Delete All Models?')) {
        for (var i = 0; i < allCurrentModels.length; i++) {
            this.instantTrackable.drawables.removeCamDrawable(allCurrentModels[i]);
        }
        allCurrentModels = [];
        World.resetAllModelValues();
         } else {
         // Do nothing!
         }
    },

    resetAllModelValues: function resetAllModelValuesFn() {
        rotationValues = [];
        scaleValues = [];
    },

    loadPathFromJsonData: function loadPathFromJsonDataFn(paths) {
    	// empty list of visible markers
    	World.modelPaths = []
    	allModelImgSources = []
    	for (var i = 0; i < paths.length; i++) {
    	World.modelPaths.push(paths.model)
    	allModelImgSources.push(paths.model)
    	}
        $("#inputs").empty();
        for(var i=0;i<allModelImgSources.length;i++){
            $("#inputs").append("<input data-id=" + i + " class='tracking-model-button list-group-item' type='image' src="+allModelImgSources[i]+" />");
        }
        $("#inputs").append("<input id='tracking-model-reset-button' class='tracking-model-button list-group-item' type='image' src='assets/buttons/trash.png' onclick='World.resetModels()' />");
    	this.createOverlays();
   	}
};
/*
World.init();*/