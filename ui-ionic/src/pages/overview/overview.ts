import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular'
import { PopoverController } from 'ionic-angular';
import { DoverlayPage } from '../device/doverlay/doverlay';
import { DpopoverPage } from '../device/dpopover/dpopover';
import { OwpopoverPage } from '../owpopover/owpopover';
import { WebSocketSubject } from 'rxjs/observable/dom/WebSocketSubject';
import { Websocket } from '../websocket/websocket';
import { LoginPage } from '../login/login';


// interface Edge {
//   id: number,
//   name: string,
//   producttype: "mini" | "pro" | ""
// }

@Component({
  selector: 'page-overview',
  templateUrl: 'overview.html'
})
export class OverviewPage {

  // private edges: Edge[] = [];

  // private metadata = {
  //   "edges": [
  //     {
  //       "id": "0",
  //       "name": "fems0",
  //       "comment": "FEMS",
  //       "producttype": "mini",
  //       "role": "admin",
  //       "online": true
  //     },
  //     {
  //       "id": "1",
  //       "name": "fems1",
  //       "comment": "FEMS1",
  //       "producttype": "pro",
  //       "role": "admin",
  //       "online": true
  //     },
  //     {
  //       "id": "2",
  //       "name": "fems2",
  //       "comment": "FEMS2",
  //       "producttype": "mini",
  //       "role": "admin",
  //       "online": true
  //     }
  //   ]
  // }

  private socket: WebSocketSubject<any>;

  ionViewDidLoad() {
    // for (let edge of this.metadata.edges) {
    //   this.edges.push({
    //     id: edge.id,
    //     name: edge.name,
    //     comment: edge.comment,
    //     producttype: edge.producttype,
    //     role: edge.role,
    //     online: edge.online
    //   })
    //   console.log("EDGE", edge);
    // }
    // console.log(this.metadata.edges);
    // // parse metadata


    // console.log(this.edges);
  }

  showSearchBar: boolean = false;

  [x: string]: any;
  constructor(public navCtrl: NavController, public popoverCtrl: PopoverController, public websocket: Websocket) {
   
  }

  itemTapped(event) {
    this.navCtrl.push(DoverlayPage);
  }
  presentPopover(myEvent) {
    let popover = this.popoverCtrl.create(OwpopoverPage);
    popover.present({
      ev: myEvent
    });
  }

  logout() {
    this.websocket.logout();
    this.navCtrl.push(LoginPage);
  }


}