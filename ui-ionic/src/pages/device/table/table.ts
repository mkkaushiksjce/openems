import { Component, Input } from '@angular/core';
import { IonicPage, NavController, NavParams } from 'ionic-angular';
import { Subject } from 'rxjs/Subject';

import { Utils } from '../../shared/service/utils';
import { DefaultTypes } from '../../shared/service/defaulttypes';
import { CurrentDataAndSummary } from '../../deviceconfig/currentdata';

@Component({
  selector: 'page-table',
  templateUrl: 'table.html',
})
export class TablePage {
  
      constructor(public navCtrl: NavController, public navParams: NavParams) {
        }
          
  @Input()
  public currentData: CurrentDataAndSummary;

  @Input()
  public config: DefaultTypes.Config;
          ionViewDidLoad() {
            console.log('ionViewDidLoad Tab2Page');
        
}
  }