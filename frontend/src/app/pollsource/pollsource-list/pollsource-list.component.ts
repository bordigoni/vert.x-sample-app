import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {PollSource} from '../pollsource.model';
import {ActivatedRoute, Router} from '@angular/router';
import 'rxjs/add/operator/switchMap'
import {PollsourceService} from '../pollsource.service';
import {ClientService} from '../../client/client.service';
import {Client} from '../../client/client.model';

@Component({
  selector: 'app-pollsource-list',
  templateUrl: './pollsource-list.component.html',
  styleUrls: ['./pollsource-list.component.css']
})
export class PollsourceListComponent implements OnInit {

  private pollSources$: Observable<Array<PollSource>>;
  private client$: Observable<Client>;
  private clientId: string;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private clientService: ClientService,
              private service: PollsourceService) {
  }

  ngOnInit() {
    this.pollSources$ = this.route.paramMap.switchMap(params => {
      this.clientId = params.get('clientId');
      return this.service.getAll(this.clientId);
    });
    this.pollSources$.subscribe(ps => this.client$ = this.clientService.get(this.clientId));
  }

  remove(pollsource: PollSource) {
    this.service.remove(pollsource.clientId, pollsource.id)
      .subscribe(() => this.pollSources$ = this.service.getAll(this.clientId));
  }

  back() {
    this.router.navigate(['/client']);
  }

}
