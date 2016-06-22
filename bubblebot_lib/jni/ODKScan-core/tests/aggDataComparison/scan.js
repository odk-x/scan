'use strict';
//Using mongo to jsonifiy csv data:
//mongoimport -d mydb -c aggdata --type csv --file aggdata.csv --headerline
//mongoexport --db mydb --collection aggdata --jsonArray --out aggdata.json

var fs = require('fs');
var path = require('path');
var _ = require('underscore');

//Walk the given directory and collect all the ODK Scan output.
var walk = function(dir, done) {
  var results = [];
  fs.readdir(dir, function(err, list) {
    if (err) return done(err);
    var pending = list.length;
    if (!pending) return done(null, results);
    list.forEach(function(file) {
      var filepath = dir + '/' + file;
      if (file === 'output.json') {
        fs.readFile(filepath, 'utf8', function (err, data) {
          if (err) return done(err);
          var pageName = path.basename(dir);
          var name = pageName.replace("(page2)", '');
          var nameComponents = name.split('_');
          var time = nameComponents[2].replace(/-/g, ':');
          var timestamp = new Date(nameComponents[1] + ' ' + time + ' UTC+0200');

          var formObject = Object.create({
            __name__ : name,
            __page__ : pageName.match("(page2)") ? 2 : 1,
            __dataset__ : path.basename(path.dirname(dir)),
            __timestamp__ : timestamp,
            __path__ : filepath
          });

          JSON.parse(data).fields.forEach(function(field){
            //Skip fields that are not processed by ODK Scan:
            if(!("value" in field)) return;
            var flabel = field.name;
            if(flabel == "provincia") return;
            if(flabel == "distrito") return;
            if(flabel == "communidade") return;
            if(flabel == "APE_name") return;
            if(flabel == "mes") return;
            if(flabel == "ano") return;

            formObject[field.name] = field.value;
          });
          results.push(formObject);
          if (!--pending) done(null, results);
        });
        return;
      }

      fs.stat(filepath, function(err, stat) {
        if (stat && stat.isDirectory()) {
          walk(filepath, function(err, res) {
            if (err) return done(err);
            results = results.concat(res);
            
            if (!--pending) done(null, results);
          });
        } else {
          if (!--pending) done(null, results);
        }
      });
    });
  });
};

walk('../MozExperiment_out', function(err, pages) {
  if (err) console.log(err);

  //Combine both pages of each form into single json objects.
  var scanForms = _.values(_(pages).groupBy('__name__')).map(function(pages){
    if(pages.length == 2) {
      pages[1].__bothPages__ = true;
      return _.extend(pages[0], pages[1]);
    }
    console.log(pages[0].__name__);
    return _.extend(pages[0]);
  });

  //Filter out single page forms...
  scanForms = _.where(scanForms, {__bothPages__: true});

  fs.readFile('aggdata.json', 'utf8', function (err, data) {
    if (err) return console.log(err);
    var aggData = JSON.parse(data);

    var totalCheckboxFields = 0;
    var totalCorrectCheckboxFields = 0;
    var totalNumericFields = 0;
    var totalCorrectNumericFields = 0;
    var combinedDiffs = {};

    //Iterate though all the forms and try to correlate
    //ODK Scan outputs with the data from ODK Aggregate.
    scanForms.forEach(function(scanForm){
      var minTimeDiff = Infinity;
      //Time match doesn't work because many correlated forms have large time differences.
      var timeMatch;
      var mostMatchingFields = 0;
      var fieldMatch;
      aggData.forEach(function(aggForm){

        var matchingFields = _.reduce(_.pairs(scanForm), function(sum, field){
          if(_.isUndefined(field[1])) {
            return sum;
          } else if(_.isNumber(field[1])) {
            return field[1] === aggForm[field[0]] ? sum + 1 : sum;
          } else {
            return sum;
          }
        }, 0);

        if(matchingFields > mostMatchingFields){
          mostMatchingFields = matchingFields;
          fieldMatch = aggForm;
        }

        var differenceInCounts = _.reduce(_.pairs(scanForm), function(sum, field){
          if(_.isUndefined(field[1])) {
            return sum;
          } else if(_.isNumber(field[1])) {
            return Math.abs(field[1] - aggForm[field[0]]) + sum;
          } else {
            return sum;
          }
        }, 0);

        var timeDiff = Math.abs(new Date(scanForm.__timestamp__) - new Date(aggForm.xformendtime));
        if(timeDiff < minTimeDiff){
          minTimeDiff = timeDiff;
          timeMatch = aggForm;
        }

      });

      //Check if the correlations changed since the last time this script ran.
      //Correlations are tracked by modifying the aggData.json file at the end of this script.
      //By default the previous correlation is used if the correlations changed,
      //but you can modify the branch condition below to generate new correlations.
      //I ran this script initially with the GT data to ensure the best possible correlations.
      var prevMatch = _.where(aggData, {__previousMatch__: scanForm.__name__})[0];
      if(prevMatch !== fieldMatch) {
        if(true){
          console.log("Current match differs from the previous match. Using previous match...");
          fieldMatch = prevMatch;
        } else {
          //Remove the previous previous match
          _.where(aggData, {__previousMatch__: scanForm.__name__}).forEach(function(match){
            match.__previousMatch__ = null;
          });
          //Annotate agg data with previous match
          fieldMatch.__previousMatch__ = scanForm.__name__;
        }
      }

      //Time Difference:
      var xformFinishTimeDelta = ( new Date(fieldMatch.xformendtime) - scanForm.__timestamp__ ) / (60 * 60 * 1000);
      //Error:
      var numericFieldDiffs = {};
      var checkboxErrors = 0;
      var checkboxFields = 0;

      _.pairs(scanForm).forEach(function(field){
        //Ignore the fields I added.
        if(field[0].slice(0,2) === "__") return;
        var otherValue = fieldMatch[field[0]];
        if(_.isUndefined(otherValue)){ 
          console.log(field[0]);
        }
        if(_.isUndefined(field[1])) {
          //noop
        } else if(_.isNumber(field[1])) {
          //The aggregate data has some random nulls peppered thoughout it.
          //I'm treating them as 0s
          if(otherValue === "null") {
            otherValue = 0;
          }
          var diff = field[1] - otherValue;
          if(_.isNaN(diff)) console.log(field[1], otherValue);
          numericFieldDiffs[diff] = (diff in numericFieldDiffs) ? numericFieldDiffs[diff] + 1 : 1;
        } else if(_.isString(field[1])) {
          if(otherValue != field[1]) checkboxErrors++;
          checkboxFields++;
        }
      });

/*
      if(xformFinishTimeDelta > 550 || xformFinishTimeDelta < 0){
        //There is one form with a slightly negative time
        //It's text fields check out though so I'm not sure what the deal is...
        //Everything seems to spot
        console.log("strange time", xformFinishTimeDelta, fieldMatch);
        console.log(scanForm.__name__);
      }

      console.log({
        "Difference in hours between scan time and xform finish time":xformFinishTimeDelta,
        "Number of errors in checkbox fields":checkboxErrors,
        "Accuracy of numeric fields":numericFieldDiffs,
        //fieldMatch: fieldMatch,
        //scanForm: scanForm.__name__
      });
*/ 
      totalCheckboxFields += checkboxFields;
      totalCorrectCheckboxFields += checkboxFields - checkboxErrors;

      totalCorrectNumericFields += numericFieldDiffs[0];
      totalNumericFields += _.values(numericFieldDiffs).reduce(function(a, b) {
          return a + b;
      });

      _.pairs(numericFieldDiffs).forEach(function(diff){
        var amnt = Math.abs(diff[0]);
        if(!(amnt in combinedDiffs)) {
          combinedDiffs[amnt] = diff[1];
        } else {
          combinedDiffs[amnt] += diff[1];
        }
      });

    });
/*
    _.pairs(combinedDiffs).forEach(function(diff){
      console.log("revised", ",", diff[0],",", diff[1]);
    });
*/
    console.log({
      totalCorrectCheckboxFields: totalCorrectCheckboxFields,
      totalIncorrectCheckboxFields: totalCheckboxFields - totalCorrectCheckboxFields,
      totalCorrectNumericFields: totalCorrectNumericFields,
      totalIncorrectNumericFields: totalNumericFields - totalCorrectNumericFields,
      "% checkbox fields correct": totalCorrectCheckboxFields / totalCheckboxFields,
      "% bubble tally fields correct" : totalCorrectNumericFields / totalNumericFields
    });


    fs.writeFile('aggdata.json', JSON.stringify(aggData), function(err) {
        if(err) {
            console.log(err);
        }
    });

  });
});
