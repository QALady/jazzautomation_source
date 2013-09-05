///////////////////////////////////////////////////////////////////////
// backbone application for jazz-automation
// copyright - 2013~2014 jazzautomation.com
// -------------------------------------------------------------------
///////////////////////////////////////////////////////////////////////

$(function(){
  ///////////////////
  // global function
  // ---------------
  //////////////////
  
  var LoadJSToHead = function(jsname, onload) {
      var docHead = document.getElementsByTagName('head')[0];
      var s = document.createElement('script');
      s.setAttribute('type','text/javascript');
      s.setAttribute('src',jsname);   
      if (!!onload)
      {
        s.addEventListener ("load", onload, false);        
      }
      docHead.appendChild(s);    
  }
  
  ////////////////////
  // models
  // ----------------
  ///////////////////
  
  // all test dao elements model
  var Settings = Backbone.RelationalModel.extend();

  var Given = Backbone.RelationalModel.extend({
    relations: [{
        type: Backbone.HasOne,
        key: 'settings',
        relatedModel: Settings
      }    
    ]
  });
  
  var Background = Backbone.RelationalModel.extend({
    relations: [{
        type: Backbone.HasOne,
        key: 'given',
        relatedModel: Given
      }    
    ]
  });
  
  var Expect = Backbone.RelationalModel.extend();
  
  var Action = Backbone.RelationalModel.extend();

  var And = Backbone.RelationalModel.extend({
    relations: [{
        type: Backbone.HasMany,
        key: 'actions',
        relatedModel: Action
      }    
    ]
  });
  
  var Then = Backbone.RelationalModel.extend({
    relations: [{
        type: Backbone.HasMany,
        key: 'expects',
        relatedModel: Expect
      }    
    ]
  });  
  
  var Scenario = Backbone.RelationalModel.extend({
    relations: [{
        type: Backbone.HasOne,
        key: 'given',
        relatedModel: Given
      },{
        type: Backbone.HasMany,
        key: 'ands',
        relatedModel: And
      },{
        type: Backbone.HasOne,
        key: 'then',
        relatedModel: Then
      }   
    ]

  });
  
  var Feature = Backbone.RelationalModel.extend({
    relations: [{
        type: Backbone.HasMany,
        key: 'scenarios',
        relatedModel: Scenario
      },{
        type: Backbone.HasOne,
        key: 'background',
        relatedModel: Background
      }    
    ],
    
    getNumOfScenarios : function() {
      return _.size(this.get("scenarios"));      
    }
    
  });
 
  // all test results model
  var ActionResult = Backbone.RelationalModel.extend({
    relations: [{
        type: Backbone.HasOne,
        key: 'and',
        relatedModel: And
      },{
        type: Backbone.HasOne,
        key: 'action',
        relatedModel: Action
      }    
    ]
  });   

  var ExpectResult = Backbone.RelationalModel.extend();
    
  var ScenarioResult = Backbone.RelationalModel.extend({
    relations: [{
        type: Backbone.HasMany,
        key: 'actionResults',
        relatedModel: ActionResult
      },{
        type: Backbone.HasMany,
        key: 'expectResults',
        relatedModel: ExpectResult
      },{
        type: Backbone.HasOne,
        key: 'scenario',
        relatedModel: Scenario
      }     
    ]
  });
  
  var FeatureResult = Backbone.RelationalModel.extend({  
    relations: [{
        type: Backbone.HasMany,
        key: 'scenarioResults',
        relatedModel: ScenarioResult
      },{
        type: Backbone.HasOne,
        key: 'feature',
        relatedModel: Feature
      }    
    ]
  });
  
  var SuiteResult = Backbone.RelationalModel.extend({
    relations: [{
        type: Backbone.HasMany,
        key: 'featureResults',
        relatedModel: FeatureResult
    }]
  });
  
  var SuiteResultList = Backbone.Collection.extend({
    // Reference to this collection's model.
    model: SuiteResult,
  });  
  
  // create a SuiteLight model to hold result list from data.js
  var SuiteLight = Backbone.Model.extend();
    
  var SuiteLightList = Backbone.Collection.extend({
    // Reference to this collection's model.
    model: SuiteLight,

  });
  
  ///////////////////
  // views
  // ----------------
  ///////////////////

    var ScenarioResultView = Backbone.View.extend({
    tagName: "div",
    template: _.template($('#scenario-template').html()),      

    initialize: function() {
      this.listenTo(this.model, 'change', this.render);
    },

    events: {
        "click .expandPanel"   : "toggleText",      
//      "dblclick .view"  : "edit",
//      "click a.destroy" : "clear",
//      "keypress .edit"  : "updateOnEnter",
//      "blur .edit"      : "close"
    },

    // Re-render the titles of the todo item.
    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      return this;
    },

    toggleText:function (){
      var signValue = this.$('.expandPanel').html();
      if (signValue == "+") 
      {
        this.$('.expandPanel').html("-");
        this.$('.scenarioContents').css({'display': "block"});      
      }
      else
      {
        this.$('.expandPanel').html("+");
        this.$('.scenarioContents').css({'display': "none"});   
      }
    }        
  });

  var FeatureResultView = Backbone.View.extend({
    tagName: "section",
    template: _.template($('#feature-template').html()),      

    initialize: function() {
      this.scenarioResults = this.model.get("scenarioResults");      
      this.listenTo(this.model, 'change', this.render);      
    },

    // Re-render the titles of the todo item.
    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      for (var i=0; i<_.size(this.scenarioResults); i++)
      {
        var scenarioResultView = new ScenarioResultView ({model: this.scenarioResults.at(i)});        
        this.$(".featureDetails").append(scenarioResultView.render().el); 
      }
      return this;
    },        
  });

   var SuiteResultSummaryView = Backbone.View.extend({
    tagName: "div",
    template: _.template($('#features-summary-template').html()),      

    initialize: function() {   
      this.listenTo(this.model, 'change', this.render);     
    },

    // Re-render the titles of the todo item.
    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      return this;
    },        
  });   
  
  var SuiteLightView = Backbone.View.extend({
    tagName:  "div",

    template: _.template($('#summary-template').html()),
    
    initialize: function() {
      this.listenTo(this.model, 'change', this.render);
    },
    
    // Re-render the titles of the todo item.
    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      return this;
    },    
  });

  //////////////////////
  // The Application
  // -------------------
  //////////////////////
  var suiteLightList = new SuiteLightList;
  var currentSuiteLight;
  var suiteResultList = new SuiteResultList;
  var suiteResult = new SuiteResult;
    
  // Our overall **AppView** is the top-level piece of UI.
  var AppView = Backbone.View.extend({

    // Instead of generating a new element, bind to the existing skeleton of
    // the App already present in the HTML.
    el: $("#jazz_report"),
        
    initialize: function() {
      this.footer = this.$('footer');
      this.main = $('#main');
      this.timePerformed = $('#timePerformed');
      this.duration = $('#overallDuration');
      suiteLightList = new SuiteLightList(data);      
      currentSuiteLight = suiteLightList.at(suiteLightList.length-1);
      var suiteLightview = new SuiteLightView ({model: currentSuiteLight});
      this.$("#projectSummary").append(suiteLightview.render().el)        
      LoadJSToHead("data/" + currentSuiteLight.get("name") + ".js", this.completeDataLoad);
    },
        
        
    completeDataLoad : function() {
      suiteResult = new SuiteResult(result);
      var suiteResultSummaryView = new SuiteResultSummaryView ({model: suiteResult});
      $("#featuresSummary").append(suiteResultSummaryView.render().el);

      for (var i=0; i< _.size(suiteResult.get("featureResults")); i++) {
        var featureResult =  suiteResult.get("featureResults").at(i);
        featureResult.set("numOfScenarios", featureResult.get("feature").getNumOfScenarios());
        var featureResultView = new FeatureResultView ({model: featureResult});
        $("#features").append(featureResultView.render().el);    
      };
    },
    
  

    render: function() {
  
    },    

  });

  // Finally, we kick things off by creating the **App**.
  var App = new AppView;

});
