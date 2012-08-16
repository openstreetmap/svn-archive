/*
File: mootools-cal-public.js
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
		data: {'id_item':id_item,'month':month,'year':year,'lang':lang},
		evalScripts:true,
		onRequest: function() { 
			el.set('html','<img class="img_loading_month" src="'+img_loading_month+'">');					//	loading image
		},
		onSuccess: function(response) {
			el.set('html',response);
			/*
			$('the_months').getElements('.weekend').each(function(el) {
				//	NOTE - CAN't change bg color as this will mess with states!!!!!!
			});
			*/
			if(clickable_past=="off"){
				document.id('the_months').getElements('.past').each(function(el) {
					el.set('opacity','0.6');
					//	NOTE - CAN't change bg color as this will mess with states!!!!!!
				});
			}

		}
	}).send();
}
//	make the dates clickable
function activate_dates(){
	//	 add custom events here - eg to update booking form
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
			var this_date=el.get('id');
			var data=this_date.split('_');
			load_calendar(el,data[0],data[1]);
		});
		
		//	once drawn, make calendars clickable
		activate_dates();
		
		//	calendar next and back buttons
		calendar_nav();
	}	
});