import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular'
import { PopoverController } from 'ionic-angular';
import { DoverlayPage } from '../device/doverlay/doverlay';
import { DpopoverPage } from '../device/dpopover/dpopover';
import { OwpopoverPage } from '../owpopover/owpopover';
import { WebSocketSubject } from 'rxjs/observable/dom/WebSocketSubject';
import { Websocket } from '../websocket/websocket';
import { LoginPage } from '../login/login';


interface Edge {
  id: number,
  name: string,
  producttype: "mini" | "pro" | ""
}

@Component({
  selector: 'page-overview',
  templateUrl: 'overview.html'
})
export class OverviewPage {

  private edges: Edge[] = [
    {
      id: 0,
      name: "fems1",
      producttype: "mini"
    },
    {
      id: 1,
      name: "fems2",
      producttype: "pro"
    },
    {
      id: 2,
      name: "fems3",
      producttype: "pro"
    },
    {
      id: 3,
      name: "fems4",
      producttype: "pro"
    },
    {
      id: 4,
      name: "fems5",
      producttype: "mini"
    },
    {
      id: 5,
      name: "fems6",
      producttype: "pro"
    },
    {
      id: 6,
      name: "fems7",
      producttype: "mini"
    }
  ];

  private metadata = {
    "edges": [
      {
        "id": 0,
        "name": "fems0",
        "comment": "FEMS",
        "producttype": "",
        "role": "admin",
        "online": true
      },
      {
        "id": 1,
        "name": "fems1",
        "comment": "FEMS1",
        "producttype": "",
        "role": "admin",
        "online": true
      }
    ]
  }

  private socket: WebSocketSubject<any>;

  ionViewDidLoad() {
    for(let edge of this.metadata.edges) {
      console.log("EDGE", edge);
    }
    console.log(this.metadata.edges);
    // parse metadata

    this.edges.push(    {
      id: 7,
      name: "fems8",
      producttype: "mini"
    })
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