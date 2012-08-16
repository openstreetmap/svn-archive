/*
File: mootools-cal-admin.js
Author: cbolson.com 
Script: availability calendar
Version: 3.03
Url: http://www.ajaxavailabilitycalendar.com
Date Created: 2009-07-20
Date Modified: 2010-01-30
*/



//	load calendar for month and year
function load_calendar(el,month,year){
	var req = new Request({
		async:false,	//	freeze browser whilst getting data - this way the elements are ready to be "clicked" :)
		method: 'get',
		url: url_ajax_cal,
		data: {
			'id_item':id_item,
			'month':month,
			'year':year,
			'lang':lang
		},
		evalScripts:true,
		onRequest: function() { 
			el.set('html','<img class="img_loading_month" src="'+img_loading_month+'">');					//	loading image
		},
		onSuccess: function(response) {
			el.set('html',response);
			$('the_months').getElements('.weekend').each(function(el) {
				//	NOTE - CAN't change bg color as this will mess with states!!!!!!
			});
			if(clickable_past=="off"){
				$('the_months').getElements('.past').each(function(el) {
					el.set('opacity','0.6');
					//	NOTE - CAN't change bg color as this will mess with states!!!!!!
				});
			}

		}
	}).send();
}

//	highlight elements - set as function so as to be able to remove it when clicked :)
var highlight_it = function(){ 
	//	fade all items in month except current
	this.getParent().getChildren('li.clickable').erase(this).tween('opacity',0.4);
}

var unhighlight_it = function(){ 
	//fade all items back in
	this.getParent().getChildren('li.clickable').tween('opacity',1);
}

var update_state = function(){
	var date_num=this.get('html');
	 var el=document.id(''+this.id+'');
	 //el.removeEvent('mouseover', highlight_it); //	stop the highlighting messgin things up
	el.erase('class');
	update_calendar(this,date_num);
}

//	make the dates clickable
function activate_dates(){
	//	define clickable (dates)
	var clickables=$$('li.clickable').setStyle('cursor', 'pointer').addEvent('mousedown', update_state);
	
	//	optional events
	if(date_hover){
		clickables.addEvent('mouseover', highlight_it).addEvent('mouseout', unhighlight_it);
	}
	/*
	if(event.shift){
		clickable.addEvent('mouseover', update_state);
	}*/
}

//	when a date is clicked - call ajax to update state
function update_calendar(el,date_num){
	var el=document.id(''+el.id+'');
	var id_pre_state="";
	//	check for default new state if defined
	if(document.id('id_predefined_state')){
		id_pre_state=$('id_predefined_state').get('value');
	}
	
	//	get module via ajax			
	var req = new Request({
		method: 'get',
		url: url_ajax_update,
		data: {
			'id_item':id_item,
			'the_date':el.id.replace("date_",""),
			'lang':lang,
			'id_state':id_pre_state
		},
		evalScripts:true,
		onRequest: function() { 
			//	loading image				
		},
		onSuccess: function(response) {
			//	split response to get class,date and desc
			tmp=response.split('|');
			
			//	add returned class
			el.addClass(tmp[0]);			
			
			//	change title to reflect new state
			el_title=el.getProperty('title');
			new_title=tmp[1]+" - "+tmp[2];
			el.setProperty('title',new_title);
			
			//	show message
			if(show_message)	new_msg(tmp[1]+" "+tmp[2]);
		}
	}).send();
}

//	messages
function new_msg(msg){
	msg_state=document.id("ajax_message");
	msg_state.set('html',msg);
	msg_state.highlight('#FF3300');
}

//	calendar navigation buttons
function calendar_nav(){
	$$('#cal_controls img').each(function(img) {
		//	thanks to http://davidwalsh.name/mootools-image-mouseovers 
		var src = img.getProperty('src');
		img.setStyle('cursor','pointer');
		var extension = src.substring(src.lastIndexOf('.'),src.length)
		img.addEvent('mouseenter', function() { img.setProperty('src',src.replace(extension,'_over' + extension)); });
		img.addEvent('mouseleave', function() { img.setProperty('src',src); });
		img.addEvent('click', function() {
			var type=img.getParent().get('id');
			if(type=='cal_prev'){
				//	get each calendar and calculate new date
				$$('div.load_cal').each(function(el){
					var this_date=el.getFirst().id;
					var data=this_date.split('_');
					
					//	convert to numeric
					cur_month	= parseFloat(data[0]);
					new_year	= parseFloat(data[1]);
					
					new_month=(cur_month-months_to_show);
					if(new_month<1){
						//	reset month and add 1 year
						new_month=(new_month+12);
						new_year=(new_year-1);
					}
					load_calendar(el,new_month,new_year);
				});
			}else if(type=='cal_next'){
				//	get each calendar and calculate new date
				$$('div.load_cal').each(function(el){
					var this_date=el.getFirst().id;
					var data=this_date.split('_');
					
					//	convert to numeric
					cur_month	= parseFloat(data[0]);
					new_year	= parseFloat(data[1]);
					
					new_month=(cur_month+months_to_show);
					if(new_month>12){
						//	reset month and add 1 year
						new_month=(new_month-12);
						new_year=(new_year+1);
					}
					load_calendar(el,new_month,new_year);
				});
			}
			//	once drawn, make calendars clickable
			activate_dates();
		});
	});
}





window.addEvent('domready', function() {
	//	load initial calendars
	if($$('.load_cal')){
		$$('.load_cal').each(function(el){
			var data=el.get('id').split('_');
			load_calendar(el,data[0],data[1]);
		});
		
		//	once drawn, make calendars clickable
		activate_dates();
		
		//	calendar next and back buttons
		calendar_nav();
	}	
});