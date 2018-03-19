import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams } from 'ionic-angular';

@Component({
  selector: 'page-table',
  templateUrl: 'table.html',
})
export class TablePage {
  
      constructor(public navCtrl: NavController, public navParams: NavParams) {
        }
          
          ionViewDidLoad() {
            console.log('ionViewDidLoad Tab2Page');
        
}
  }