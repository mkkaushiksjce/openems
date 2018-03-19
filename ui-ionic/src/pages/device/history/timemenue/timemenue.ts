import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams, ViewController, Events } from 'ionic-angular';
import { CalendarComponentOptions } from 'ion2-calendar';


@Component({
  selector: 'page-timemenue',
  templateUrl: 'timemenue.html',
})
export class TimemenuePage {

  calendarRef(arg0: any): any {
    throw new Error("Method not implemented.");
  }
  _toastWrap(arg0: any, arg1: any): any {
    throw new Error("Method not implemented.");
  }
  dateRange: { from: string; to: string; };
  type: 'string'; // 'string' | 'js-date' | 'moment' | 'time' | 'object'
  optionsRange: CalendarComponentOptions = {
    pickMode: 'range'
  };

  constructor(public navCtrl: NavController, public navParams: NavParams, public viewCtrl: ViewController) {
  }

  closeIcon=true;
  doneIcon=true;
  ionViewDidLoad() {
    console.log('ionViewDidLoad ModalPage');
  }

  dismiss() {
    this.viewCtrl.dismiss();
  }




}
